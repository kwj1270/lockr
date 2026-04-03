#!/bin/bash
# Domain Test Coverage Guard
# AggregateRoot의 비즈니스 메서드 중 테스트가 없는 것을 찾아 리포트

PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/official/lockr/domain"
TEST="$PROJECT_ROOT/src/test/java/com/official/lockr/domain"

echo "# Domain Test Coverage Report"
echo ""
echo "> $(date '+%Y-%m-%d %H:%M')"
echo ""

# AggregateRoot를 상속하는 파일 찾기
find "$SRC" -name "*.java" | while read f; do
  grep -q "extends AggregateRoot" "$f" 2>/dev/null || continue

  CLASS_NAME=$(basename "$f" .java)
  REL_PATH=$(echo "$f" | sed "s|$PROJECT_ROOT/||")

  # public 메서드 이름만 추출 (macOS 호환)
  # "public void addMember(" → "addMember"
  # "public static Club init(" → "init"
  # 생성자는 제외 (반환 타입이 없는 것)
  METHODS=$(grep -E '^\s+public\s+' "$f" | \
    grep -v "^\s*public\s\+${CLASS_NAME}\s*(" | \
    grep -oE '[a-zA-Z]+\s*\(' | \
    sed 's/\s*($//' | \
    grep -E '^[a-z]' | \
    grep -vE '^(get[A-Z]|set[A-Z]|is[A-Z]|has[A-Z]|equals|hashCode|toString|publish)' | \
    sort -u)

  [ -z "$METHODS" ] && continue

  # 해당 도메인의 테스트 파일 찾기
  DOMAIN_PATH=$(dirname "$f" | sed "s|$SRC/||" | cut -d/ -f1-2)
  TEST_FILES=$(find "$TEST/$DOMAIN_PATH" -name "*Test.java" 2>/dev/null)

  ALL_TEST_CONTENT=""
  if [ -n "$TEST_FILES" ]; then
    ALL_TEST_CONTENT=$(cat $TEST_FILES 2>/dev/null)
  fi

  UNTESTED=""
  TESTED=""
  TOTAL=0
  TESTED_COUNT=0

  for method in $METHODS; do
    TOTAL=$((TOTAL + 1))
    if [ -n "$ALL_TEST_CONTENT" ] && echo "$ALL_TEST_CONTENT" | grep -q "$method" 2>/dev/null; then
      TESTED_COUNT=$((TESTED_COUNT + 1))
      TESTED="$TESTED $method"
    else
      UNTESTED="$UNTESTED $method"
    fi
  done

  if [ -n "$UNTESTED" ]; then
    echo "### $CLASS_NAME ($REL_PATH)"
    echo "- 전체: ${TOTAL}개 | 테스트 있음: ${TESTED_COUNT}개 | **누락: $((TOTAL - TESTED_COUNT))개**"
    echo "- 테스트 없는 메서드:"
    for m in $UNTESTED; do
      echo "  - \`$m()\`"
    done
    echo ""
  fi
done

#!/bin/bash
# Domain CLAUDE.md Drift Checker (범용)
# 모든 도메인의 AggregateRoot 변경 시 해당 도메인 CLAUDE.md와 동기화 여부 확인

FILE="$CLAUDE_FILE_PATH"
[ -z "$FILE" ] && exit 0

# domain/{context}/{subdomain}/domain/ 하위 Java 파일만 대상
echo "$FILE" | grep -qE '/domain/[^/]+/[^/]+/domain/.*\.java$' || exit 0

# AggregateRoot 변경인 경우만 체크
grep -q 'extends AggregateRoot' "$FILE" 2>/dev/null || exit 0

# context 추출 (domain/{context}/{subdomain}/domain/ 에서 첫 번째 세그먼트)
CONTEXT=$(echo "$FILE" | sed -n 's|.*/domain/\([^/]*\)/[^/]*/domain/.*|\1|p')
[ -z "$CONTEXT" ] && exit 0

CLAUDEMD="src/main/java/com/official/lockr/domain/${CONTEXT}/CLAUDE.md"
[ ! -f "$CLAUDEMD" ] && exit 0

# 변경된 파일에서 클래스명 추출
CLASSNAME=$(basename "$FILE" .java)

# CLAUDE.md에서 AR 이름 검색
if ! grep -q "$CLASSNAME" "$CLAUDEMD" 2>/dev/null; then
  echo "⚠️ [CLAUDE.md Drift] '${CLASSNAME}'이 ${CONTEXT}/CLAUDE.md에 누락되었습니다. 도메인 문서 업데이트를 권장합니다."
fi

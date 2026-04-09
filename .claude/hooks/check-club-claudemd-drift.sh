#!/bin/bash
# Club CLAUDE.md Drift Checker
# club 도메인의 AggregateRoot 변경 시 CLAUDE.md와 동기화 여부 확인

FILE="$CLAUDE_FILE_PATH"
[ -z "$FILE" ] && exit 0

# club 도메인의 domain/ 하위 Java 파일만 대상
echo "$FILE" | grep -qE '/domain/club/.*/domain/.*\.java$' || exit 0

# AggregateRoot 변경인 경우만 체크
grep -q 'extends AggregateRoot' "$FILE" 2>/dev/null || exit 0

CLUB_CLAUDEMD="src/main/java/com/official/lockr/domain/club/CLAUDE.md"
[ ! -f "$CLUB_CLAUDEMD" ] && exit 0

# 변경된 파일에서 클래스명 추출
CLASSNAME=$(basename "$FILE" .java)

# CLAUDE.md의 Subdomain Map 테이블에서 AR 이름 검색
if ! grep -q "$CLASSNAME" "$CLUB_CLAUDEMD" 2>/dev/null; then
  echo "⚠️ [CLAUDE.md Drift] '${CLASSNAME}'이 club/CLAUDE.md에 누락되었습니다. Subdomain Map 업데이트를 권장합니다."
fi

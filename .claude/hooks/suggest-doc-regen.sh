#!/bin/bash
# Doc Regeneration Suggestion Hook
# domain/ 하위 Aggregate 파일 변경 시 문서 재생성을 알림

FILE="$CLAUDE_FILE_PATH"
[ -z "$FILE" ] && exit 0

# domain/{context}/{subdomain}/domain/ 하위의 Java 파일만 체크
if echo "$FILE" | grep -qE '/domain/[^/]+/[^/]+/domain/.*\.java$'; then
  if grep -q 'extends AggregateRoot\|implements DomainEvent\|enum ' "$FILE" 2>/dev/null; then
    echo "📄 도메인 모델이 변경되었습니다. docs를 갱신하려면: bash .claude/scripts/generate-domain-docs.sh"
  fi
fi
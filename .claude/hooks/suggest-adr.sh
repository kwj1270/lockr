#!/bin/bash
# ADR Suggestion Hook
# 아키텍처 변경이 감지되면 ADR 작성을 제안

FILE="$CLAUDE_FILE_PATH"
[ -z "$FILE" ] && exit 0

# build.gradle 의존성 변경 — 새 라이브러리 도입 가능성
if echo "$FILE" | grep -q 'build.gradle'; then
  if git diff --cached -- "$FILE" 2>/dev/null | grep -qE '^\+.*implementation|^\+.*api\b'; then
    echo "💡 새 의존성이 추가되었습니다. 기술 선택 이유를 ADR로 기록하는 것을 권장합니다. (/adr)"
  fi
fi

# Flyway 마이그레이션 추가 — 스키마 변경
if echo "$FILE" | grep -qE 'db/migration/V[0-9]+.*\.sql$'; then
  echo "💡 새 마이그레이션이 추가되었습니다. 스키마 설계 결정을 ADR로 기록하는 것을 검토하세요."
fi

# 새 Bounded Context 추가
if echo "$FILE" | grep -q '/domain/[^/]*/[^/]*/domain/.*\.java$'; then
  if grep -q 'extends AggregateRoot' "$FILE" 2>/dev/null; then
    # 기존에 없던 새 Aggregate인지 확인
    AGGREGATE=$(basename "$FILE" .java)
    EXISTING=$(git log --oneline --all -- "**/domain/**/${AGGREGATE}.java" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$EXISTING" = "0" ]; then
      echo "💡 새 Aggregate '$AGGREGATE'가 추가되었습니다. Aggregate 경계 결정을 ADR로 기록하는 것을 권장합니다."
    fi
  fi
fi

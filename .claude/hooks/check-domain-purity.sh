#!/bin/bash
# DDD Layer Violation Checker
# domain/ 레이어에 프레임워크 의존(jOOQ, Spring, Jakarta)이 유입되면 경고

FILE="$CLAUDE_FILE_PATH"

# domain/{context}/{subdomain}/domain/ 경로인지 확인
if echo "$FILE" | grep -q '/domain/[^/]*/[^/]*/domain/'; then
  if grep -q 'import org\.jooq\|import org\.springframework\|import jakarta\.' "$FILE" 2>/dev/null; then
    echo "⚠️ domain/ 레이어에 프레임워크 의존(jOOQ/Spring/Jakarta)이 감지되었습니다. domain 레이어는 순수 Java만 사용해야 합니다."
  fi
fi

# Service에서 jOOQ 직접 참조 확인
if echo "$FILE" | grep -q '/application/.*Service\.java$'; then
  if grep -q 'import org\.jooq' "$FILE" 2>/dev/null; then
    echo "⚠️ Service에서 jOOQ 직접 참조가 감지되었습니다. Repository 인터페이스를 통해 접근하세요."
  fi
fi

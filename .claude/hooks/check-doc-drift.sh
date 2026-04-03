#!/bin/bash
# Domain Context Doc Drift Checker
# domain/ 코드에 Event/Aggregate가 추가/삭제됐는데 docs/domains/에 반영 안 된 경우 경고

FILE="$CLAUDE_FILE_PATH"
PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"

# Event 파일이 추가/수정된 경우만 체크
if echo "$FILE" | grep -q '/domain/.*/event/.*Event\.java$'; then
  EVENT_NAME=$(basename "$FILE" .java)

  # 어떤 컨텍스트의 이벤트인지 판별
  if echo "$FILE" | grep -q '/domain/club/'; then
    DOC="$PROJECT_ROOT/docs/domains/club-context.md"
  elif echo "$FILE" | grep -q '/domain/auth/'; then
    DOC="$PROJECT_ROOT/docs/domains/auth-context.md"
  elif echo "$FILE" | grep -q '/domain/users/'; then
    DOC="$PROJECT_ROOT/docs/domains/users-context.md"
  elif echo "$FILE" | grep -q '/domain/notification/'; then
    DOC="$PROJECT_ROOT/docs/domains/notification-context.md"
  elif echo "$FILE" | grep -q '/domain/shorts/'; then
    DOC="$PROJECT_ROOT/docs/domains/club-context.md"
  elif echo "$FILE" | grep -q '/domain/home/'; then
    DOC="$PROJECT_ROOT/docs/domains/home-context.md"
  else
    exit 0
  fi

  if [ -f "$DOC" ]; then
    if ! grep -q "$EVENT_NAME" "$DOC" 2>/dev/null; then
      echo "📝 $EVENT_NAME 이 $(basename $DOC) 에 문서화되지 않았습니다. docs/domains/ 업데이트를 검토하세요."
    fi
  fi
fi

# AggregateRoot 파일이 추가된 경우
if echo "$FILE" | grep -q '/domain/.*/domain/[A-Z].*\.java$'; then
  if grep -q 'extends AggregateRoot' "$FILE" 2>/dev/null; then
    AGGREGATE_NAME=$(basename "$FILE" .java)

    if echo "$FILE" | grep -q '/domain/club/'; then
      DOC="$PROJECT_ROOT/docs/domains/club-context.md"
    elif echo "$FILE" | grep -q '/domain/auth/'; then
      DOC="$PROJECT_ROOT/docs/domains/auth-context.md"
    else
      exit 0
    fi

    if [ -f "$DOC" ]; then
      if ! grep -q "$AGGREGATE_NAME" "$DOC" 2>/dev/null; then
        echo "📝 새 Aggregate '$AGGREGATE_NAME' 이 $(basename $DOC) 에 문서화되지 않았습니다."
      fi
    fi
  fi
fi

#!/bin/bash
# Event Flow Mermaid Diagram Generator
# 코드베이스에서 Domain Event 발행/구독 관계를 추출하여 Mermaid 다이어그램 생성

PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/official/lockr"
OUTPUT="$PROJECT_ROOT/docs/event-flow.md"

cat > "$OUTPUT" << 'HEADER'
# Domain Event Flow

> Auto-generated from codebase. Do not edit manually.
> Run: `bash .claude/scripts/generate-event-flow.sh`

```mermaid
flowchart LR

    %% Bounded Contexts
    subgraph Auth[Auth]
        Auth_SignIn[SignIn]
        Auth_OIDC[OIDC]
    end
    subgraph Club[Club]
        Club_Club[Club]
        Club_Schedule[Schedule]
        Club_Chat[Chat]
        Club_Feed[Feed]
        Club_Recruitment[Recruitment]
        Club_Squad[Squad]
        Club_Lineup[Lineup]
        Club_Stats[Stats]
    end
    subgraph Others[Others]
        Users[Users]
        Notification[Notification]
        Shorts[Shorts]
    end

    %% Event Flows
HEADER

# 각 Consumer 파일에서 이벤트 구독 관계 추출
get_domain_node() {
  local filepath="$1"
  case "$filepath" in
    *club/club/*) echo "Club_Club" ;;
    *club/chat/*) echo "Club_Chat" ;;
    *club/feed/*) echo "Club_Feed" ;;
    *club/schedule/*) echo "Club_Schedule" ;;
    *club/sport/football/squad/*) echo "Club_Squad" ;;
    *club/sport/football/lineup/*) echo "Club_Lineup" ;;
    *club/recruitment/*) echo "Club_Recruitment" ;;
    *club/stats/*) echo "Club_Stats" ;;
    *auth/signin/*) echo "Auth_SignIn" ;;
    *auth/oidc/*) echo "Auth_OIDC" ;;
    *notification/*) echo "Notification" ;;
    *shorts/*) echo "Shorts" ;;
    *users/*) echo "Users" ;;
    *) echo "" ;;
  esac
}

# Consumer 파일 순회
find "$SRC/domain" \( -name "*Consumer*.java" -o -name "*EventListener*.java" \) | sort | while read consumer; do
  CONSUMER_NODE=$(get_domain_node "$consumer")
  [ -z "$CONSUMER_NODE" ] && continue

  # 이벤트 클래스 추출: "XxxEvent event" 또는 "(final XxxEvent event)" 패턴
  grep -oE '[A-Z][a-zA-Z]*Event' "$consumer" 2>/dev/null | sort -u | while read event; do
    # EventListener, TransactionalEventListener, DomainEvent 자체는 제외
    case "$event" in
      EventListener|TransactionalEventListener|DomainEvent|ApplicationEvent|ChatSseEvent) continue ;;
    esac

    # 이벤트 소스 파일 찾기
    EVENT_FILE=$(find "$SRC/domain" -name "${event}.java" -path "*/event/*" 2>/dev/null | head -1)
    [ -z "$EVENT_FILE" ] && continue

    EVENT_NODE=$(get_domain_node "$EVENT_FILE")
    [ -z "$EVENT_NODE" ] && continue
    [ "$EVENT_NODE" = "$CONSUMER_NODE" ] && continue  # 자기 자신 제외

    SHORT=$(echo "$event" | sed 's/Event$//')
    echo "    ${EVENT_NODE} -->|${SHORT}| ${CONSUMER_NODE}" >> "$OUTPUT"
  done
done

# 이벤트 화살표 중복 제거 (HEADER 부분은 보존하고, 화살표 줄만 dedup)
if [ -f "$OUTPUT" ]; then
  TEMP=$(mktemp)
  awk '/^ *[A-Z].*-->/{if(!seen[$0]++) print; next} {print}' "$OUTPUT" > "$TEMP" && mv "$TEMP" "$OUTPUT"
fi

echo '```' >> "$OUTPUT"

# Event 목록
cat >> "$OUTPUT" << 'EVENTS_HEADER'

## Event 목록

| Event | 발행 도메인 | 파일 |
|-------|-----------|------|
EVENTS_HEADER

find "$SRC/domain" -path "*/event/*Event.java" -name "*Event.java" | sort | while read f; do
  EVENT_NAME=$(basename "$f" .java)
  REL_PATH=$(echo "$f" | sed "s|$PROJECT_ROOT/||")
  DOMAIN=$(echo "$REL_PATH" | sed 's|src/main/java/com/official/lockr/domain/||' | cut -d/ -f1-2)
  echo "| $EVENT_NAME | $DOMAIN | \`$REL_PATH\` |" >> "$OUTPUT"
done

# Consumer 목록
cat >> "$OUTPUT" << 'CONSUMER_HEADER'

## Consumer 목록

| Consumer | 도메인 | 파일 |
|----------|--------|------|
CONSUMER_HEADER

find "$SRC/domain" \( -name "*Consumer*.java" -o -name "*EventListener*.java" \) | sort | while read f; do
  NAME=$(basename "$f" .java)
  REL_PATH=$(echo "$f" | sed "s|$PROJECT_ROOT/||")
  DOMAIN=$(echo "$REL_PATH" | sed 's|src/main/java/com/official/lockr/domain/||' | cut -d/ -f1-2)
  echo "| $NAME | $DOMAIN | \`$REL_PATH\` |" >> "$OUTPUT"
done

echo "✅ Event flow diagram generated: docs/event-flow.md"

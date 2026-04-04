#!/bin/bash
# Domain Documentation Generator
# 코드베이스에서 도메인 모델 정보를 추출하여 문서를 자동 생성한다.
#
# 생성 파일:
#   docs/models/event-flow.md               - Domain Event 발행/구독 흐름 다이어그램
#   docs/models/class-diagrams/_overview.md - Aggregate 관계 클래스 다이어그램
#   docs/glossary.md                        - 도메인 용어집
#   docs/invariants/{aggregate}.md          - Aggregate별 비즈니스 불변 규칙

set -euo pipefail

PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/official/lockr"

# ─── 공통 함수 ───────────────────────────────────────────────────────────────

get_domain_node() {
  local filepath="$1"
  case "$filepath" in
    *club/club/*)                  echo "Club_Club" ;;
    *club/chat/*)                  echo "Club_Chat" ;;
    *club/feed/*)                  echo "Club_Feed" ;;
    *club/fee/*)                   echo "Club_Fee" ;;
    *club/schedule/*)              echo "Club_Schedule" ;;
    *club/sport/football/squad/*)  echo "Club_Squad" ;;
    *club/sport/football/lineup/*) echo "Club_Lineup" ;;
    *club/recruitment/*)           echo "Club_Recruitment" ;;
    *club/stats/*)                 echo "Club_Stats" ;;
    *auth/signin/*)                echo "Auth_SignIn" ;;
    *auth/oidc/*)                  echo "Auth_OIDC" ;;
    *notification/*)               echo "Notification" ;;
    *shorts/*)                     echo "Shorts" ;;
    *users/*)                      echo "Users" ;;
    *)                             echo "" ;;
  esac
}

# ─── 1. Event Flow ───────────────────────────────────────────────────────────

generate_event_flow() {
  local OUTPUT="$PROJECT_ROOT/docs/models/event-flow.md"
  mkdir -p "$(dirname "$OUTPUT")"

  cat > "$OUTPUT" << 'HEADER'
# Domain Event Flow

> Auto-generated from codebase. Do not edit manually.
> Run: `bash .claude/scripts/generate-domain-docs.sh`

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
        Club_Fee[Fee]
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

  # Consumer 파일 순회하여 이벤트 구독 관계 추출
  find "$SRC/domain" \( -name "*Consumer*.java" -o -name "*EventListener*.java" \) | sort | while read -r consumer; do
    CONSUMER_NODE=$(get_domain_node "$consumer")
    [ -z "$CONSUMER_NODE" ] && continue

    # || true: grep exits 1 when no match; pipefail would abort otherwise
    grep -oE '[A-Z][a-zA-Z]*Event' "$consumer" 2>/dev/null | sort -u | while read -r event; do
      case "$event" in
        EventListener|TransactionalEventListener|DomainEvent|ApplicationEvent|ChatSseEvent) continue ;;
      esac

      EVENT_FILE=$(find "$SRC/domain" -name "${event}.java" -path "*/event/*" 2>/dev/null | head -1)
      [ -z "$EVENT_FILE" ] && continue

      EVENT_NODE=$(get_domain_node "$EVENT_FILE")
      [ -z "$EVENT_NODE" ] && continue
      [ "$EVENT_NODE" = "$CONSUMER_NODE" ] && continue

      SHORT=$(echo "$event" | sed 's/Event$//')
      echo "    ${EVENT_NODE} -->|${SHORT}| ${CONSUMER_NODE}" >> "$OUTPUT"
    done || true
  done

  # 중복 화살표 제거
  if [ -f "$OUTPUT" ]; then
    TEMP=$(mktemp)
    awk '/^ *[A-Z].*-->/{if(!seen[$0]++) print; next} {print}' "$OUTPUT" > "$TEMP" && mv "$TEMP" "$OUTPUT"
  fi

  printf '```\n' >> "$OUTPUT"

  # Event 목록
  cat >> "$OUTPUT" << 'EVENTS_HEADER'

## Event 목록

| Event | 발행 도메인 | 파일 |
|-------|-----------|------|
EVENTS_HEADER

  find "$SRC/domain" -path "*/event/*Event.java" -name "*Event.java" | sort | while read -r f; do
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

  find "$SRC/domain" \( -name "*Consumer*.java" -o -name "*EventListener*.java" \) | sort | while read -r f; do
    NAME=$(basename "$f" .java)
    REL_PATH=$(echo "$f" | sed "s|$PROJECT_ROOT/||")
    DOMAIN=$(echo "$REL_PATH" | sed 's|src/main/java/com/official/lockr/domain/||' | cut -d/ -f1-2)
    echo "| $NAME | $DOMAIN | \`$REL_PATH\` |" >> "$OUTPUT"
  done

  echo "  [1/4] Event Flow: $OUTPUT"
}

# ─── 2. Aggregate Overview ───────────────────────────────────────────────────

generate_overview() {
  local OUTPUT="$PROJECT_ROOT/docs/models/class-diagrams/_overview.md"
  mkdir -p "$(dirname "$OUTPUT")"

  {
    echo "# Aggregate Overview"
    echo ""
    echo "> Auto-generated from codebase. Do not edit manually."
    echo "> Run: \`bash .claude/scripts/generate-domain-docs.sh\`"
    echo ""
    printf '```mermaid\n'
    echo "classDiagram"

    # AggregateRoot 상속 관계
    find "$SRC/domain" -name "*.java" | sort | while read -r f; do
      if grep -qE "extends AggregateRoot" "$f" 2>/dev/null; then
        AGG=$(basename "$f" .java)
        echo "    $AGG --|> AggregateRoot"
      fi
    done

    # ID 참조 관계 파싱
    find "$SRC/domain" -name "*.java" | sort | while read -r f; do
      if grep -qE "extends AggregateRoot" "$f" 2>/dev/null; then
        AGG=$(basename "$f" .java)
        # || true: grep exits 1 when no Id fields; pipefail would abort otherwise
        grep -oE 'private final String [a-zA-Z]+Id;' "$f" 2>/dev/null | while read -r line; do
          FIELD=$(echo "$line" | grep -oE '[a-zA-Z]+Id' | sed 's/Id$//')
          # 첫 글자 대문자로
          TARGET="$(echo "${FIELD:0:1}" | tr '[:lower:]' '[:upper:]')${FIELD:1}"
          if [ "$TARGET" != "$AGG" ]; then
            echo "    $AGG ..> $TARGET : ${FIELD}Id"
          fi
        done || true
      fi
    done

    printf '```\n'
  } > "$OUTPUT"

  echo "  [2/4] Aggregate Overview: $OUTPUT"
}

# ─── 3. Glossary ─────────────────────────────────────────────────────────────

generate_glossary() {
  local OUTPUT="$PROJECT_ROOT/docs/glossary.md"

  {
    echo "# Domain Glossary"
    echo ""
    echo "> Auto-generated (자동 추출 섹션) + 수동 보완 섹션으로 구성."
    echo "> 자동 섹션은 \`bash .claude/scripts/generate-domain-docs.sh\` 로 재생성."
    echo ""
    echo "---"
    echo ""
    echo "## Aggregate Roots"
    echo ""
    echo "| Aggregate | 파일 경로 |"
    echo "|-----------|----------|"

    find "$SRC/domain" -name "*.java" | while read -r f; do
      if grep -qE "extends AggregateRoot" "$f" 2>/dev/null; then
        AGG=$(basename "$f" .java)
        REL=$(echo "$f" | sed "s|$PROJECT_ROOT/||")
        echo "| $AGG | \`$REL\` |"
      fi
    done | sort

    echo ""
    echo "## Domain Events"
    echo ""
    echo "| Event | 설명 |"
    echo "|-------|------|"

    find "$SRC/domain" -name "*.java" | while read -r f; do
      if grep -qE "implements DomainEvent" "$f" 2>/dev/null; then
        EVT=$(basename "$f" .java)
        echo "| $EVT | |"
      fi
    done | sort

    echo ""
    echo "## Enums"
    echo ""
    echo "| Enum | 값 목록 |"
    echo "|------|--------|"

    find "$SRC/domain" -name "*.java" | while read -r f; do
      if grep -qE "^public enum " "$f" 2>/dev/null; then
        ENAME=$(basename "$f" .java)
        # 열거 상수: 4칸 들여쓰기 + UPPER_CASE 토큰; || true for grep no-match
        VALS=$(grep -oE '^    [A-Z][A-Z0-9_]*' "$f" 2>/dev/null | tr -d ' ' | tr '\n' ', ' | sed 's/,$//' || true)
        echo "| $ENAME | $VALS |"
      fi
    done | sort

    echo ""
    echo "## Value Objects"
    echo ""
    echo "| Value Object | 파일 경로 |"
    echo "|--------------|----------|"

    find "$SRC/domain" -name "*.java" | while read -r f; do
      # api/, application/, infrastructure/ 레이어 제외
      if echo "$f" | grep -qE "/(api|application|infrastructure)/"; then
        continue
      fi
      if echo "$f" | grep -qE "/domain/"; then
        if grep -qE "^public (record|class) " "$f" 2>/dev/null; then
          if ! grep -qE "(extends AggregateRoot|implements DomainEvent)" "$f" 2>/dev/null; then
            if ! grep -qE "^public enum " "$f" 2>/dev/null; then
              VO=$(basename "$f" .java)
              REL=$(echo "$f" | sed "s|$PROJECT_ROOT/||")
              echo "| $VO | \`$REL\` |"
            fi
          fi
        fi
      fi
    done | sort

    echo ""
    echo "---"
    echo ""
    echo "## 수동 보완 섹션"
    echo ""
    echo "> 아래 테이블은 자동 추출되지 않는 개념 정의를 수동으로 기입한다."
    echo ""
    echo "### 유비쿼터스 언어 (Ubiquitous Language)"
    echo ""
    echo "| 용어 | 설명 |"
    echo "|------|------|"
    echo "| | |"
    echo ""
    echo "### 바운디드 컨텍스트 경계"
    echo ""
    echo "| 컨텍스트 | 책임 |"
    echo "|----------|------|"
    echo "| | |"

  } > "$OUTPUT"

  echo "  [3/4] Glossary: $OUTPUT"
}

# ─── 4. Invariants ───────────────────────────────────────────────────────────

generate_invariants() {
  local OUTDIR="$PROJECT_ROOT/docs/invariants"
  mkdir -p "$OUTDIR"

  find "$SRC/domain" -name "*.java" | sort | while read -r f; do
    if ! grep -qE "extends AggregateRoot" "$f" 2>/dev/null; then
      continue
    fi

    # throw 라인이 없으면 스킵
    if ! grep -qE "throw new Illegal(Argument|State)Exception" "$f" 2>/dev/null; then
      continue
    fi

    AGG=$(basename "$f" .java)
    # CamelCase → kebab-case
    KEBAB=$(echo "$AGG" | sed -E 's/([A-Z])/-\1/g' | sed 's/^-//' | tr '[:upper:]' '[:lower:]')
    OUTFILE="$OUTDIR/${KEBAB}.md"

    {
      echo "# ${AGG} — 비즈니스 규칙"
      echo ""
      echo "> Auto-generated from codebase. Do not edit manually."
      echo "> Run: \`bash .claude/scripts/generate-domain-docs.sh\`"
      echo ""
      echo "| 메서드 | 규칙 | 위반 시 |"
      echo "|--------|------|--------|"

      # POSIX awk (macOS 호환): 3-arg match() 미지원이므로 sub/gsub로 메서드명 추출
      awk '
        /[[:space:]](public|private|protected)[[:space:]]/ && /\(/ && !/class |interface |enum |@/ {
          line = $0
          # 메서드명: 소문자로 시작하는 식별자 바로 앞에 공백이 있는 패턴
          if (match(line, /[a-z][a-zA-Z0-9]*[[:space:]]*\(/)) {
            mname = substr(line, RSTART, RLENGTH - 1)
            gsub(/[[:space:]]/, "", mname)
            current_method = mname
          }
        }
        /throw new Illegal(Argument|State)Exception/ {
          if ($0 ~ /IllegalArgumentException/) etype = "IllegalArgumentException"
          else etype = "IllegalStateException"

          msg = ""
          line = $0
          if (match(line, /"[^"]*"/)) {
            msg = substr(line, RSTART + 1, RLENGTH - 2)
          }
          if (msg == "") msg = "(메시지 없음)"
          if (current_method == "") current_method = "(unknown)"

          print "| " current_method " | " msg " | " etype " |"
        }
      ' "$f"

    } > "$OUTFILE"
  done

  echo "  [4/4] Invariants: $OUTDIR/"
}

# ─── main ─────────────────────────────────────────────────────────────────────

main() {
  echo "Generating domain documentation..."
  echo ""

  generate_event_flow
  generate_overview
  generate_glossary
  generate_invariants

  echo ""
  echo "Done. Generated files:"
  echo "  docs/models/event-flow.md"
  echo "  docs/models/class-diagrams/_overview.md"
  echo "  docs/glossary.md"
  echo "  docs/invariants/*.md"
}

main "$@"

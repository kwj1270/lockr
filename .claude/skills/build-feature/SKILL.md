---
name: build-feature
description: "기능 개발 파이프라인. Plan → Implement → Audit 3단계를 각각 전문 에이전트에 위임하여 오케스트레이션. 기능 구현, 기능 추가, 기능 개발, 도메인 기능 빌드 시 사용. 이전 빌드 결과 수정, 감사만 재실행, 구현 재시도에도 사용. /build-feature {도메인} {기능 설명}"
argument-hint: "{도메인명} {기능 설명}"
---

# Build Feature - Plan / Implement / Audit Pipeline

기능 개발을 3개의 전문 에이전트로 분리하여 오케스트레이션하는 파이프라인 스킬입니다.

```
[오케스트레이터 (이 스킬)]
├── Phase 1: Plan     → feature-planner 에이전트 (설계만)
├── Phase 2: Implement → feature-executor 에이전트 (구현만)
└── Phase 3: Audit    → domain-audit 에이전트 (감사만)
```

역할 분리 이유:
- **Plan 품질**: 구현 부담 없이 순수하게 설계에 집중
- **컨텍스트 분리**: 구현 중 코드가 감사 판단을 오염시키지 않음
- **검증 독립성**: 자기가 짠 코드를 자기가 감사하지 않음

## 사용법

```
/build-feature {도메인명} {기능 설명}
```

예시:
```
/build-feature notification 읽음 처리 벌크 API 추가
/build-feature club 클럽 가입 승인 워크플로우
```

---

## Phase 0: 컨텍스트 확인

`_workspace/` 디렉토리에서 기존 산출물을 확인하여 실행 모드를 결정합니다.

| 조건 | 실행 모드 | 동작 |
|------|----------|------|
| `_workspace/{feature}/plan.md` 없음 | **초기 실행** | Phase 1부터 전체 진행 |
| plan.md 있음 + 사용자가 "수정" 요청 | **구현 재실행** | Phase 2부터 (기존 Plan 기반) |
| plan.md 있음 + 사용자가 "감사만" 요청 | **감사만 재실행** | Phase 3만 실행 |
| plan.md 있음 + 사용자가 "계획 수정" 요청 | **계획 재수립** | Phase 1부터 (기존 Plan 참고) |

**feature 이름 결정**: 도메인명과 기능 설명에서 kebab-case로 생성 (예: `notification-bulk-read`, `club-approval-workflow`)

---

## Phase 1: Plan (feature-planner 에이전트)

feature-planner 에이전트를 호출하여 계획서를 생성합니다.

```
Agent(
  subagent_type="feature-planner",
  prompt="
    도메인: {도메인명}
    기능: {기능 설명}
    출력 경로: _workspace/{feature}/plan.md

    위 기능의 구현 계획서를 생성하여 출력 경로에 저장해줘.
  "
)
```

Fallback (커스텀 에이전트 실패 시):
```
Agent(
  subagent_type="general-purpose",
  prompt="
    .claude/agents/feature-planner.md 를 읽고 그 에이전트의 역할과 절차를 그대로 따라서
    도메인: {도메인명}, 기능: {기능 설명}의 구현 계획서를 생성해줘.
    출력 경로: _workspace/{feature}/plan.md
  "
)
```

### 사용자 승인 대기

> **CRITICAL**: 에이전트가 생성한 계획서를 읽어 사용자에게 제시하세요.
> 반드시 사용자의 승인을 받은 후 Phase 2로 진행하세요.
> 수정 요청이 있으면 계획서를 직접 수정하거나 feature-planner를 재호출하세요.

---

## Phase 2: Implement (feature-executor 에이전트)

사용자가 계획을 승인하면 feature-executor 에이전트를 호출합니다.

```
Agent(
  subagent_type="feature-executor",
  prompt="
    계획서 경로: _workspace/{feature}/plan.md
    이 계획서를 읽고 그대로 구현해줘.
  "
)
```

Fallback:
```
Agent(
  subagent_type="general-purpose",
  prompt="
    .claude/agents/feature-executor.md 를 읽고 그 에이전트의 역할과 절차를 그대로 따라서
    _workspace/{feature}/plan.md 계획서를 구현해줘.
  "
)
```

수정/피드백이 있는 경우:
```
Agent(
  subagent_type="feature-executor",
  prompt="
    계획서 경로: _workspace/{feature}/plan.md
    수정 요청: {사용자 피드백 내용}
    기존 구현에서 위 피드백 부분만 수정해줘.
  "
)
```

---

## Phase 3: Audit (domain-audit 에이전트)

구현 완료 후 domain-audit 에이전트를 호출하여 감사를 실행합니다.

```
Agent(
  subagent_type="domain-audit",
  prompt="{도메인명} 도메인을 감사해줘"
)
```

Fallback:
```
Agent(
  subagent_type="general-purpose",
  prompt="
    .claude/agents/domain-audit.md 를 읽고 그 에이전트의 역할과 절차를 그대로 따라서
    {도메인명} 도메인을 감사해줘.
  "
)
```

### 결과 처리

| 감사 결과 | 조치 |
|----------|------|
| 전체 PASS | Phase 완료 → 사용자에게 결과 보고 |
| WARN만 있음 | 사용자에게 WARN 목록 제시, 수정 여부 확인 |
| FAIL 있음 | feature-executor에 FAIL 항목 전달하여 수정 → 재감사 (최대 3회) |

### 재감사 루프

```
감사 → FAIL 발견 → executor에 수정 위임 → 재감사 → ... (최대 3회)
```

3회 재감사 후에도 FAIL이 남으면:
- 남은 FAIL 목록을 사용자에게 보고
- 자동 수정 불가 사유 설명
- 사용자 판단 요청

### 도메인 CLAUDE.md 업데이트

감사 통과 후, 구현된 내용을 반영하여 도메인 CLAUDE.md를 업데이트합니다:
- 새 Aggregate/VO/Event 추가
- 새 비즈니스 규칙 추가
- Cross-Domain 의존 변경 반영
- Repository 메서드 추가

---

## 완료 보고

모든 Phase가 끝나면 사용자에게 최종 결과를 보고합니다:

```markdown
## Build Feature 완료: {기능명}

### 생성/수정된 파일
- {파일 목록}

### 감사 결과
| 기준 | 판정 |
|------|------|
| CQRS 준수 | PASS |
| 도메인 순수성 | PASS |
| Aggregate 경계 | PASS |
| 이벤트 일관성 | PASS |
| CLAUDE.md 정합성 | PASS |

### CLAUDE.md 업데이트
- (변경 내용 요약)
```

## 테스트 시나리오

### 정상 흐름
```
/build-feature notification 읽음 처리 벌크 API 추가
→ Phase 0: _workspace/ 확인 → 초기 실행
→ Phase 1: feature-planner → plan.md 생성 → 사용자 승인
→ Phase 2: feature-executor → 구현
→ Phase 3: domain-audit → PASS → 완료 보고
```

### 감사 실패 → 수정 흐름
```
→ Phase 3: domain-audit → FAIL (도메인 순수성 위반)
→ feature-executor에 FAIL 항목 전달 → 수정
→ 재감사 → PASS → 완료 보고
```

### 후속 실행 (감사만)
```
/build-feature notification 감사만 재실행
→ Phase 0: plan.md 존재 확인 + "감사만" 키워드
→ Phase 3만 실행
```

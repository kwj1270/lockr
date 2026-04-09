---
name: build-feature
description: "기능 개발 파이프라인. Plan → Implement → Audit 3단계를 자동 오케스트레이션. /build-feature {도메인} {기능 설명}"
argument-hint: "{도메인명} {기능 설명}"
---

# Build Feature - Plan / Implement / Audit Pipeline

기능 개발을 3단계로 오케스트레이션하는 파이프라인 스킬입니다.

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

## Phase 1: Plan (Planner)

### 1.1 도메인 맥락 파악

대상 도메인의 CLAUDE.md를 읽어 현재 상태를 파악합니다:

```
src/main/java/com/official/lockr/domain/{context}/CLAUDE.md
```

파악할 내용:
- 현재 Aggregate, VO, Event 구조
- 기존 비즈니스 규칙
- Cross-Domain 의존 관계
- Repository 메서드

### 1.2 tactical-design 패턴 참조

`.claude/skills/tactical-design/SKILL.md`를 읽어 구현 패턴을 확인합니다.
세부 구현 방법이 아닌 **어떤 패턴을 적용할지**만 결정합니다.

### 1.3 구현 계획 생성

아래 형식으로 계획을 생성하여 **사용자에게 제시**합니다:

```markdown
## Feature Plan: {기능명}

### 변경 요약
- (1~3문장으로 무엇을 왜 추가/변경하는지)

### 영향 범위
| 레이어 | 파일 | 변경 유형 |
|--------|------|----------|
| domain/ | {파일명} | 신규/수정 |
| application/ | {파일명} | 신규/수정 |
| api/ | {파일명} | 신규/수정 |
| infrastructure/ | {파일명} | 신규/수정 |

### Acceptance Criteria
- [ ] (검증 가능한 기준 1)
- [ ] (검증 가능한 기준 2)
- [ ] (검증 가능한 기준 3)

### 설계 결정
- Aggregate 경계: (변경 있으면 명시)
- 새 Event: (있으면 명시)
- Cross-Domain 의존: (있으면 명시)
```

### 1.4 사용자 승인 대기

> **CRITICAL**: 계획을 제시한 후 반드시 사용자의 승인을 받으세요.
> 승인 없이 구현으로 넘어가지 마세요.
> 사용자가 수정을 요청하면 계획을 업데이트하고 다시 제시하세요.

---

## Phase 2: Implement (Generator)

사용자가 계획을 승인하면 구현을 시작합니다.

### 2.1 구현 순서

tactical-design 스킬의 패턴을 따라 아래 순서로 구현합니다:

1. **Domain 레이어** (순수 Java)
   - AggregateRoot / VO / Entity 수정 또는 생성
   - Domain Event 추가 (record implements DomainEvent)
   - Repository 인터페이스 메서드 추가

2. **Application 레이어**
   - Command 객체 생성 (Java Record)
   - UseCase 인터페이스 생성 (1 인터페이스 = 1 메서드)
   - Service 구현 (UseCase implements, Repository 인터페이스만 의존)

3. **Infrastructure 레이어**
   - JOOQ Repository 구현
   - domain() / toDomain() 매핑
   - publish(domainEventPublisher) 호출

4. **API 레이어**
   - Command Api (POST/PUT/DELETE → UseCase 주입)
   - Query Api (GET → Configuration → DAO.ctx())
   - Request/Response DTO (toCommand() 포함)
   - Consumer (이벤트 구독, 필요시)

5. **Migration** (필요시)
   - Flyway SQL 마이그레이션 파일

### 2.2 구현 규칙

- domain/ 패키지에 Spring/jOOQ/Jakarta import 금지
- ID는 ULID 사용 (`UlidUtils.generateUlid()`)
- 팩토리 메서드에서 `addEvent()` 호출
- Repository.save()에서 `aggregate.publish(domainEventPublisher)` 호출
- Request DTO에 `toCommand()` 메서드 포함

---

## Phase 3: Audit (Evaluator)

구현 완료 후 `domain-audit` 에이전트를 호출하여 감사를 실행합니다.

### 3.1 감사 실행

Agent를 사용하여 domain-audit를 실행합니다:
```
Agent(subagent_type="domain-audit", prompt="{도메인명} 도메인을 감사해줘")
```

### 3.2 결과 처리

| 감사 결과 | 조치 |
|----------|------|
| 전체 PASS | Phase 완료 → 사용자에게 결과 보고 |
| WARN만 있음 | 사용자에게 WARN 목록 제시, 수정 여부 확인 |
| FAIL 있음 | FAIL 항목 수정 후 재감사 (최대 3회) |

### 3.3 재감사 루프

```
감사 → FAIL 발견 → 수정 → 재감사 → ... (최대 3회)
```

3회 재감사 후에도 FAIL이 남으면:
- 남은 FAIL 목록을 사용자에게 보고
- 자동 수정 불가 사유 설명
- 사용자 판단 요청

### 3.4 도메인 CLAUDE.md 업데이트

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

# 0007. Chaos Monkey 도입 — Outbox/Inbox 회복력 학습 검증

- 상태: 승인됨 (학습 목적)
- 날짜: 2026-04-28
- 도메인: global/outbox, global/inbox

## 컨텍스트

ADR-0006으로 Outbox/Inbox 범용 이벤트 인프라가 도입되었다. 이 인프라의 핵심 가치 제안은:

- at-least-once 전달 보장 (publish 실패 시 retry, markProcessed 실패 시 재발행)
- 트랜잭션 일관성 (Aggregate save와 outbox INSERT 원자성, inbox INSERT와 doHandle 원자성)
- 멱등성 가드 (inbox-based)
- stuck entry 모니터링 (MAX_RETRY 도달 시 ERROR 로그)

이 회복력 보장이 **실제로 작동하는지** 결정론적 단위 테스트만으로는 검증의 한계가 있다. 특히:

- 랜덤한 실패 타이밍에서의 재시도 분포
- 누적 retry로 stuck 임계 도달 시점
- markProcessed 실패 후 재발행 + 컨슈머 멱등성 가드 상호작용
- latency 발생 시 `@Scheduled(fixedDelay)` 폴링 루프 동작

운영 트래픽이 적고 단일 노드로 시작하는 현재 단계에서는, 학습 목적의 카오스 주입이 **"패턴이 약속한 대로 동작하는지 손에 익히는"** 가성비 높은 검증 도구가 될 수 있다.

## 결정

`de.codecentric:chaos-monkey-spring-boot:3.2.2` 도입.

**활성화 범위 — local 프로파일 전용**:
- `application.yml`: `chaos.monkey.enabled: false` (안전장치, 모든 환경 기본 비활성)
- `application-local.yml`: `chaos.monkey.enabled: true` + Outbox/Inbox 메서드 타겟팅

**타겟 한정 — `watchedCustomServices`**:
범용 watcher(component/repository/service)는 모두 비활성. `watchedCustomServices`로 Outbox/Inbox 핵심 메서드만 명시 타겟팅하여 다른 도메인 컴포넌트는 영향받지 않음.

타겟 메서드:
- `OutboxProcessor.process` — 폴링 루프 자체에 latency/exception 주입
- `OutboxDomainEventPublisher.publish` — Aggregate save 시점의 outbox INSERT 경로
- `JOOQOutboxRepository.{insert,findUnprocessed,markProcessed}` — Outbox CRUD 경로
- `JOOQInboxRepository.insertIfAbsent` — 멱등 가드 INSERT
- `IdempotentEventHandler.handle` — 컨슈머 진입점

**Assault 정책 — 보수적**:
- `level: 5` — 5번 호출 중 1번 주입 (학습 목적 빈도)
- `latency: 1000~3000ms` — 폴링 100ms 주기 대비 충분한 지연
- `exceptions: active` — 랜덤 RuntimeException (retry 동작 검증)
- `killApplicationActive: false` — JVM 종료는 학습 초기 단계에서 과함

**런타임 토글**:
`management.endpoints.web.exposure.include`에 `chaosmonkey` 추가 — `POST /prefer/chaosmonkey/disable`로 즉시 비활성화 가능.

## 안전장치

| 위협 | 방어 |
|------|------|
| 운영 환경 활성화 사고 | `application.yml` 기본 `enabled: false` + local 프로파일에서만 override |
| 다른 도메인 영향 | `watchedCustomServices` 명시 타겟팅, 범용 watcher 비활성 |
| 일반 개발 흐름 방해 | actuator endpoint로 즉시 disable 가능 |
| JVM 강제 종료 | `killApplicationActive: false` |
| CI/Test 영향 | 테스트 시 `local` 프로파일 미적용 → 자동 비활성 |

## 검증 시나리오 (Phase 2 — 도입 후 학습 진행)

1. **publish 실패 → retry 동작**
   - 기대: `OutboxProcessor.publish`에 exception 주입 → `incrementRetry` → 다음 폴링에서 재시도 → MAX_RETRY 도달 시 stuck 진입 → 5분 monitorStuck ERROR 로그
2. **markProcessed 실패 → 재발행 + 멱등성**
   - 기대: `markProcessed` 실패 → 다음 폴링에서 동일 entry 재선택 → publish 중복 → 컨슈머 inbox 가드로 skip
3. **latency 누적 → 폴링 직렬화 확인**
   - 기대: `process`에 1-3초 latency → `fixedDelay` 다음 호출은 완료 후 100ms → 동일 batch 중복 처리 없음

## 대안 비교

| 대안 | 평가 |
|------|------|
| **단위/통합 테스트만** | 결정론적 시나리오는 우수. 다만 랜덤 타이밍 + 누적 시나리오는 표현 한계 |
| **Toxiproxy** (네트워크 레벨) | DB connection drop 등 검증 가능. 그러나 Spring 컨텍스트 외부 도구 도입 부담 |
| **Spring Boot Chaos Monkey** ✅ | Spring AOP 기반, 의존성 1개, profile 기반 격리, actuator 토글 — 학습 단계 ROI 최적 |
| **운영 카오스 (Litmus, Chaos Mesh)** | 분산/멀티 노드 전환 후 재평가. 현재 over-engineering |

## 결과

- 의존성 1개 추가 (`chaos-monkey-spring-boot`)
- 설정 분리 — 운영 안전성 보장
- Outbox/Inbox 회복력 검증 인프라 확보
- 분산/멀티 노드 전환 시점에 watcher 범위 + assault 정책 재평가 (멀티 노드 전환 ADR과 함께)

## 참고

- [Spring Boot Chaos Monkey](https://codecentric.github.io/chaos-monkey-spring-boot/)
- ADR-0006 — Outbox/Inbox 범용 이벤트 인프라 + CloudEvents
- `docs/guides/domain-events/03-outbox-consideration.md`

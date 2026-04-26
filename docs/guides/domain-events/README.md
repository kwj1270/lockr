# Domain Event 설계 가이드

lockr-server에서 Domain Event를 발행·소비할 때 참고하는 가이드. 일반적 DDD 관습과 프로젝트 고유 규칙을 함께 정리한다.

## 구성

| # | 문서 | 다루는 질문 |
|---|------|-----------|
| 1 | [consumer-placement.md](01-consumer-placement.md) | 이벤트 Consumer는 어느 레이어에 둬야 하는가? |
| 2 | [transaction-and-events.md](02-transaction-and-events.md) | 트랜잭션을 엮어야 할 때 이벤트를 쓰는 게 맞는가? adapter-in / application 중 어디? |
| 3 | [outbox-consideration.md](03-outbox-consideration.md) | Transactional Outbox Pattern은 언제·어디에 도입하는가? |
| 4 | [case-verification-consent.md](04-case-verification-consent.md) | 워크드 예시 — "검증 → 동의" 시나리오 설계 |

## 핵심 규칙 요약

### 1) Consumer 위치

| 역할 | 위치 |
|------|------|
| 다른 Aggregate의 UseCase 호출 (in-process) | `application/` |
| 외부 브로커(Kafka/RabbitMQ) → 내부 UseCase 브릿지 | `infrastructure/adapter/in/messaging/` |
| 외부 시스템 릴레이 (FCM, Email, SSE) | `infrastructure/` |
| Read Model Projection | `infrastructure/projection/` |
| Saga / Process Manager | `application/saga/` |

lockr-server는 in-process Spring Events이므로 **기본은 `application/`**. 외부 I/O만 `infrastructure/`.

### 2) 트랜잭션 엮기가 필요한가?

```
publisher의 tx에 참여해야 하는가?
├─ No (eventual OK)  → 이벤트 유지, application/ 에 consumer
└─ Yes (atomic 필요) → 이벤트 쓰지 말고 UseCase 직접 오케스트레이션
     └─ 부득이 이벤트 유지 시 application/ 에만 (adapter-in 금지)
```

### 3) Outbox 도입 기준

| 조건 | Outbox 권장 |
|------|-----------|
| 외부 시스템 연동 + 사용자 체감 (FCM, 알림) | ✅ |
| 유실이 늦게 드러남 (수기 검토, 배치) | ✅ |
| in-process 크로스 서브도메인 동기화 | ❌ (과함) |
| 정합성 수동 복구 가능 | ❌ |

## 관련 문서

- [ADR](../../adr/README.md) — 설계 결정 기록
- [event-flow.md](../../models/event-flow.md) — 현재 이벤트 발행/구독 다이어그램
- [tactical-design Skill](../../../.claude/skills/tactical-design/SKILL.md) — 전술적 설계 패턴

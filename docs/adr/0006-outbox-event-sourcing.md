# ADR-0006: Outbox/Inbox 범용 이벤트 인프라 + CloudEvents 포맷

- **상태**: 승인됨
- **날짜**: 2026-04-23
- **관련 레이어**: global/outbox, global/inbox, global/ddd

## 컨텍스트

도메인 이벤트 전파는 현재 Spring `@EventListener`(+ `@Transactional(REQUIRES_NEW)`) 기반 sync 구조다. 이 구조는 다음을 보장하지 못한다.

1. **이벤트 전파 신뢰성 부재** — 리스너가 실패해도 트랜잭션 격리(REQUIRES_NEW)로 예외가 전파되지 않는다. 반대로 **실패한 이벤트를 재처리할 경로도 없다.** 공식 outbox 패턴 부재.
2. **중복 이벤트 방어 부재** — 컨슈머 재처리 시나리오에서 멱등성을 보장할 inbox 장치가 없다.
3. **이벤트 포맷 표준 부재** — 향후 다른 BC 또는 외부 시스템(Kafka/NATS/SQS 등)으로 이벤트를 흘릴 때 참조할 공통 스키마가 없다.
4. **원자성 불확실** — Aggregate UPSERT와 이벤트 발행이 하나의 트랜잭션에 묶여야 하지만, 리스너 내부 DB 조작이 REQUIRES_NEW로 분리되면서 "상태는 반영됐는데 후속 처리가 누락되는" 케이스가 가능.

위 문제를 단일 인프라로 해결할 구조를 결정한다.

## 고려한 선택지

### 선택지 1: 현재 구조 유지 + 리스너 실패 로깅 강화

- 장점: 변경 범위 최소.
- 단점: 신뢰성/중복 방어/포맷 표준 모두 미해결. 근본 해결책이 아님.

### 선택지 2: Outbox/Inbox 범용 인프라 + CloudEvents 포맷 (채택)

`global` 레이어에 `domain_event_outbox` / `domain_event_inbox` 테이블을 구축하고, payload는 CloudEvents 1.0 JSON 포맷으로 저장한다. `OutboxProcessor`가 `@Scheduled(fixedDelay=100ms)`로 polling하여 `ApplicationEventPublisher`에 릴레이. Consumer는 `IdempotentEventHandler` 베이스를 상속해 inbox 기반 중복 방어.

- 장점: 이벤트 at-least-once + idempotency 확보. 외부 전파 시 CloudEvents 표준으로 호환성.
- 장점: 범용 인프라이므로 전 도메인에 동일 패턴 적용 가능.
- 장점: `IntegrationDomainEvent` 마커 인터페이스로 **점진 마이그레이션** 가능. 기존 `DomainEvent` 그대로 둔 채 Outbox 대상만 선별적으로 전환.
- 단점: 구현 범위가 크고 전제 인프라(테이블 2개, Processor, Publisher 구현체 교체, Inbox 베이스 클래스)가 필요.
- 단점: 이벤트 처리가 sync → async 전환되어 최대 100ms 지연 발생.

### 선택지 3: 선택지 2에서 Repository가 Outbox DAO를 직접 주입

기존 `DomainEventPublisher` 인터페이스를 우회하고, 각 Repository가 `OutboxDAO`를 직접 주입받아 `save()` 내부에서 INSERT.

- 장점: 레이어 1개 감소. 명시적.
- 단점: 기존 `aggregate.publish(publisher)` 전 도메인 컨벤션 깨짐. 다른 도메인 Outbox 전환 시 모든 Repository를 다시 수정해야 함.

## 결정

**선택지 2를 채택한다.** 구성은 다음과 같다.

### 1. Outbox/Inbox 테이블

- `domain_event_outbox`(V20): `event_id UNIQUE`, `payload JSON`(CloudEvents 1.0 전체 직렬화), `processed`, `retry_count`.
- `domain_event_inbox`(V21): `(event_id, consumer_name)` PK. consumer는 **symbolic name**(예: `sms.verification-history`)을 사용해 리팩터링에 강건하게 유지.

### 2. DomainEvent 계층 구조

```
DomainEvent (in-process 이벤트)
   ↑
IntegrationDomainEvent (outbox 대상 마커)
  — eventId / eventType / source / aggregateId / occurredAt 필수
```

- 기존 `DomainEvent` 인터페이스 유지 (breaking change 없음).
- `IntegrationDomainEvent`를 구현한 이벤트만 outbox 경로로 흐른다. 그 외는 기존 sync 발행 유지.

### 3. Publisher 단일 교체

`OutboxDomainEventPublisher implements DomainEventPublisher`(@Primary)로 기존 Bean 대체.

```java
public void publish(DomainEvent event) {
    if (event instanceof IntegrationDomainEvent ie) {
        CloudEventEnvelope envelope = envelopeMapper.toEnvelope(ie);
        outboxRepository.insert(new OutboxEntry(envelope));    // outbox INSERT
    } else {
        applicationEventPublisher.publishEvent(event);          // 기존 sync 경로
    }
}
```

Repository는 기존 `aggregate.publish(domainEventPublisher)` 컨벤션 유지. 도메인 레이어는 Outbox 개념을 인지하지 않는다.

### 4. CloudEvents 1.0 포맷 + envelope/data 분리

```json
{
  "specversion": "1.0",
  "id": "<ULID>",
  "source": "lockr://{bc}/{subdomain}",
  "type": "com.official.lockr.{bc}.{subdomain}.{aggregate}.{action}",
  "subject": "{aggregateId}",
  "time": "<ISO8601>",
  "datacontenttype": "application/json",
  "data": { /* 순수 도메인 payload — envelope 필드 중복 제거 */ }
}
```

- `eventId` / `eventType` / `source` / `aggregateId` / `occurredAt`은 envelope 레벨로 승격 (쿼리·인덱스 대상).
- `data` 영역은 **순수 도메인 payload**만 담는다. envelope 필드와 중복 직렬화하지 않는다.
- 역직렬화 시 `CloudEventEnvelope.toEvent()`가 envelope 메타데이터를 `data`에 재주입해 원본 이벤트 객체를 복원.

### 5. OutboxProcessor

`@Scheduled(fixedDelay=100ms)` 단일 노드 폴링.

```
SELECT * FROM domain_event_outbox
  WHERE processed = false AND retry_count < 10
  ORDER BY seq
  LIMIT 100;
for each:
  try: publishEvent(envelope.toEvent(clazz, mapper)) → markProcessed
  catch: incrementRetry (processed=false 유지)
```

- `ORDER BY seq`로 Aggregate 내 순서 보장 (PK auto-increment).
- at-least-once: markProcessed 전에 크래시하면 재발행.
- retry ≥ 10 row는 모니터링 쿼리로 감시 (DLQ는 후속 과제).

### 6. Inbox 멱등성

컨슈머는 `IdempotentEventHandler<E>` 베이스를 상속한다.

```
1. inbox.insertIfAbsent(eventId, consumerName)
   → false(이미 처리됨)면 return
2. doHandle(event) 실행
3. 전체를 @Transactional로 묶어 실패 시 inbox도 롤백
```

`consumerName`은 **symbolic name**(`{bc}.{purpose}` 형식)으로 리팩터링에 강건.

### 7. OutboxEventTypeRegistry

`eventType` 문자열 → `Class<? extends IntegrationDomainEvent>` 매핑. 각 도메인은 `@Configuration`에서 자신의 이벤트 타입을 등록한다.

## 근거

- **Publisher 교체 방식(선택지 2)은 "다른 도메인 추후 적용" 요구에 자동 대응한다.** 신규 도메인에서 `IntegrationDomainEvent`만 구현하면 Outbox 경로가 자동 활성화된다. Repository는 무변경.
- **CloudEvents는 향후 Kafka/NATS/SQS/EventBridge 등 외부 메시징 도입 시 재직렬화 없이 그대로 전파 가능하다.** 초기 단계부터 표준 포맷으로 기록해두면 전환 비용이 0에 수렴한다.
- **envelope/data 분리는 포맷 자기기술성을 유지한다.** envelope은 라우팅·검색·조회용 메타데이터, `data`는 도메인 페이로드로 역할이 명확히 분리되고 중복 직렬화가 제거된다.
- **ADR 0004(Simplified CQRS)와 충돌하지 않는다.** Command 경로의 트랜잭션 경계가 AR 저장 + Outbox INSERT를 원자적으로 묶고, Query 경로는 영향 없음.

## 결과

### 긍정

- `event_id UNIQUE` + inbox `(event_id, consumer_name) UNIQUE`로 **at-least-once + idempotency** 보장.
- 전 도메인에 Outbox 인프라가 준비됨. 다른 BC 전환 시 `IntegrationDomainEvent` 구현만으로 편입.
- CloudEvents 1.0 표준 채택으로 외부 메시지 브로커 도입 시 페이로드 변환 비용 제로.
- 도메인 이벤트 인터페이스 breaking change 없음 (기존 `DomainEvent` 유지, 신규는 `IntegrationDomainEvent`).
- envelope/data 분리로 페이로드 크기 축소, 스키마 모호성 해소.

### 부정

- 이벤트 처리가 sync → async로 전환되어 최대 100ms 지연. **동기 응답에 이벤트 결과가 반영되어야 하는 케이스는 Outbox 부적합**.
- Outbox Processor 단일 노드 가정. 향후 멀티노드 확장 시 분산 락 또는 `FOR UPDATE SKIP LOCKED` 도입 필요.
- RDB 부하 증가(폴링 쿼리 100ms 주기). 현 MVP 트래픽 규모에선 감수 가능.

### 주의

- Repository `save()`는 반드시 `@Transactional`이어야 한다. AR UPSERT와 Outbox INSERT가 동일 트랜잭션에서 원자적으로 묶여야 한다.
- `IntegrationDomainEvent.eventId`는 AR의 `addEvent()` 시점에 ULID로 생성. 재시도 시에도 동일 이벤트 객체가 재사용되어야 outbox `UNIQUE` 제약이 중복 차단 역할을 한다.
- `retry_count >= 10` row에 대한 알람 경로는 후속 과제(MVP에선 max retry 운영 모니터링으로 우회).
- Inbox row는 시간 경과 시 무한 증가. `processed_at` 기반 정리 배치 필요.

## Follow-up

- 도메인별 Outbox 편입 로드맵 (`auth/sms`, `club/fee`, `users` 등).
- `OutboxEventTypeRegistry` 자동 등록 — 이벤트 타입이 10+로 늘어 누락 위험이 커지면 `@EventType` 어노테이션 + classpath 스캔 기반 자동 디스커버리 도입.
- Outbox Processor 멀티노드 지원 — 단계적 도입:
    1. 멀티 인스턴스 운영 직전: `ShedLock` (Redis 기반)으로 `@SchedulerLock` 적용 → leader-only 폴링.
    2. 처리량이 단일 노드 한계에 근접 시: `SELECT ... FOR UPDATE SKIP LOCKED` + `claim_at` 컬럼 도입으로 row 단위 분산 처리.
- max retry 초과 이벤트의 DLQ(Dead Letter Queue) 정책.
- Inbox `processed_at` 기반 오래된 레코드 정리 배치.
- Outbox `processed=true` row 아카이빙/정리 배치.

# Transactional Outbox Pattern 도입 검토

## 현재 구조와 한계

### 현재

```
Repository.save() 내부
  └─ @Transactional 커밋
       └─ @TransactionalEventListener(AFTER_COMMIT)
            └─ @Async + @Transactional(REQUIRES_NEW)
                 └─ Consumer (FCM, DB 업데이트, SSE...)
```

### 보장되는 것

- Publisher tx 커밋 후에만 consumer 실행
- Consumer 실패가 publisher에 영향 주지 않음

### 보장 안 되는 것

| 리스크 | 발생 조건 |
|-------|---------|
| 이벤트 유실 | 커밋 직후 앱 크래시 (JVM 종료, 배포 재시작) |
| Consumer 실패 시 재시도 없음 | 현재 `log.error`만 찍고 끝 |
| At-least-once 보장 없음 | 이벤트가 단 한 번도 처리 안 될 수 있음 |
| 순서 보장 없음 | `@Async`로 병렬 처리 |

## Outbox Pattern이 제공하는 것

### 핵심 아이디어

이벤트를 **publisher와 같은 tx**로 `outbox_events` 테이블에 저장 → 별도 디스패처가 폴링해서 발행.

```
┌─ Tx 1 (원자) ────────────────┐
│  Aggregate.save()             │
│  outbox.save(event)           │  ← 같은 tx
└───────────────────────────────┘

별도 스케줄러
  → outbox_events 폴링
  → consumer 호출 or 브로커 발행
  → 성공 시 outbox 레코드 delete / mark as sent
```

### 얻는 것

- **At-least-once 보장** — 앱 크래시에도 이벤트 살아남음
- **재시도 가능** — consumer 실패 시 outbox에 남아있어 다음 폴링에서 재시도
- **순서 보장(옵션)** — `created_at` 순서 처리
- **브로커 전환 시 코어 불변** — publisher는 outbox만 씀, 디스패처만 바꾸면 됨

### 비용

- 추가 테이블 (`outbox_events`)
- 디스패처 인프라 (Spring `@Scheduled` 또는 별도 컴포넌트)
- 폴링 지연 (보통 1~5초)
- 멱등성 설계 필요 (at-least-once 특성)

## 도입 기준

| 기준 | 도입 권장 |
|------|---------|
| 외부 시스템 연동 + 사용자 체감 | ✅ |
| 유실이 늦게 드러남 (운영자 미인지) | ✅ |
| FCM, Email, SMS 등 외부 통지 | ✅ |
| 금전/법적 이슈와 연결 | ✅ |
| in-process 크로스 서브도메인 동기화 | ❌ (과함) |
| 정합성 수동 복구 가능 | ❌ |
| 솔로/소규모 프로젝트 + 저위험 | ❌ (오버엔지니어링) |

## lockr-server 도입 우선순위

전면 도입은 과하다. **선별적 도입**이 합리적.

### 1순위 — 외부 I/O 알림

| Consumer | 이벤트 | 유실 시 영향 |
|----------|-------|------------|
| `FeeNotificationEventConsumer` | `UnpaidFeeNotifiedEvent` | "미납 알림 안 옴" 컴플레인 |
| `ScheduleNotificationEventConsumer` | `CreatedScheduleEvent` | "일정 알림 안 옴" 컴플레인 |

→ FCM은 외부 시스템 + 사용자 체감 + 유실이 조용히 넘어감. **outbox 도입 가치 명확**.

### 2순위 — 상태 변경에 영향 주는 이벤트

예: `VerificationEnteredManualReviewEvent` → Consent expiresAt 연장 (수기 검토 7일)

→ 유실 시 Consent가 원래 24h에 만료 = UX 직격탄, 운영자가 늦게 알아챔.

### 도입 보류

- `FeedEventConsumer` (Schedule → Feed 생성) — 같은 DB, 수동 복구 가능
- `ClubEventConsumer` (Application → Member) — 관리자 개입으로 복구 가능
- `ChatEventListener(sse)` — 실시간성이 본질, outbox 부적합 (5초 지연이면 의미 없음)

## 구현 스케치

### 1) 테이블

```sql
CREATE TABLE outbox_events (
    id            VARCHAR(26)     NOT NULL PRIMARY KEY,  -- ULID
    event_type    VARCHAR(100)    NOT NULL,
    payload       JSON            NOT NULL,
    aggregate_id  VARCHAR(26)     NOT NULL,
    status        VARCHAR(20)     NOT NULL,  -- PENDING, PROCESSING, SENT, FAILED
    retry_count   INT             NOT NULL DEFAULT 0,
    last_error    TEXT            NULL,
    created_at    DATETIME(6)     NOT NULL,
    processed_at  DATETIME(6)     NULL,
    INDEX idx_status_created (status, created_at)
);
```

### 2) Publisher 측 (DomainEventPublisher 변경)

```java
// 기존 SpringDomainEventPublisher를 OutboxDomainEventPublisher로 교체
@Component
public class OutboxDomainEventPublisher implements DomainEventPublisher {
    private final OutboxRepository outboxRepo;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(DomainEvent event) {
        outboxRepo.save(new OutboxEvent(
            UlidUtils.generateUlid(),
            event.getClass().getSimpleName(),
            objectMapper.writeValueAsString(event),
            event.aggregateId(),
            OutboxStatus.PENDING,
            LocalDateTime.now()
        ));
    }
}
```

→ Publisher tx에 outbox 레코드까지 묶임 (같은 tx). 여기가 핵심.

### 3) 디스패처

```java
@Component
public class OutboxEventDispatcher {
    private final OutboxRepository outboxRepo;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(fixedDelay = 2000)  // 2초 폴링
    @Transactional
    public void dispatch() {
        List<OutboxEvent> events = outboxRepo.findPending(100);
        for (OutboxEvent e : events) {
            try {
                DomainEvent domainEvent = deserialize(e);
                applicationEventPublisher.publishEvent(domainEvent);
                outboxRepo.markSent(e.id());
            } catch (Exception ex) {
                outboxRepo.markFailed(e.id(), ex.getMessage());
            }
        }
    }
}
```

### 4) Consumer 측 변화

**없음**. Consumer는 기존처럼 `@TransactionalEventListener`/`@EventListener`로 받으면 됨.

단 **멱등성**은 필수: 같은 이벤트가 2번 와도 안전해야 한다 (retry 시 at-least-once).

## 멱등성 설계 팁

- 이벤트 ID를 기반으로 `processed_events` 테이블 체크
- 또는 상태 전이 자체를 멱등으로 (`markPaid` 예: 이미 PAID면 no-op)
- 연장 로직처럼 누적적인 경우: `extensionApplied` 플래그로 중복 방어

## 단계적 도입 로드맵

| 단계 | 범위 | 판단 기준 |
|------|-----|---------|
| Phase 0 | 현재 유지 | 트래픽 작고 유실 UX 영향 제한적 |
| Phase 1 | FCM 계열만 outbox | 사용자 알림 컴플레인 발생 시 |
| Phase 2 | 상태 변경 이벤트 (Consent 연장 등) | 정합성 요구 상승 시 |
| Phase 3 | 전면 outbox + Kafka 전환 | MSA 분리 / 스케일아웃 시 |

## ADR 기록

전면 도입이 아니어도 "왜 일부만 도입하는가"는 ADR로 남길 가치가 있음.

- [ADR 템플릿](../../adr/TEMPLATE.md) 참고
- 제목 예: "FCM 알림 이벤트에 Outbox Pattern 선별 도입"
- 내용: 현재 유실 리스크 / 적용 범위 / 비용 / 대안 비교

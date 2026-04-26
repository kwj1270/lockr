# 트랜잭션과 Domain Event

## 핵심 명제

> **트랜잭션을 엮어야 한다면 그것은 "이벤트"가 아니라 "UseCase 조합(Orchestration)"이다.**

이벤트는 본래 **eventual consistency**를 전제로 한다. 같은 트랜잭션에 묶어야 한다면 설계 선택이 잘못된 것일 가능성이 높다.

## adapter-in에 same-tx consumer를 두면 안 되는 이유

### 1) 외부 브로커는 원천적으로 publisher tx에 참여 불가

Kafka / RabbitMQ / SNS는 publisher가 이미 커밋한 후에 메시지가 전달된다. "같은 tx"라는 개념이 성립하지 않는다. Adapter-in은 정의상 "자기 tx를 새로 연다".

### 2) tx 경계 관리는 Application의 책임

Hexagonal / Clean 정석에서 `@Transactional` 경계는 UseCase 진입점 = Application Service에 있다. Adapter가 자기 tx를 열고 다른 서비스 호출까지 묶으면 레이어 책임이 번진다.

### 결론

**adapter-in 컨슈머는 본질적으로 `REQUIRES_NEW`에 가깝다. publisher와 tx를 엮는 건 의미상 불가능하거나 부적절하다.**

## same-tx consume이 필요할 때의 선택지

DDD 정석 관점:

> "원자적 일관성이 필요하다면 같은 Aggregate다. Cross-Aggregate는 eventual consistency."
> — Vaughn Vernon

현실적으로 불가피한 경우엔 3가지 선택지가 있고, **이벤트는 보통 정답이 아니다**.

| 상황 | 권장 | 위치 |
|------|------|------|
| 두 Aggregate가 **반드시 원자적** | 이벤트 지우고 **Application Service에서 직접 오케스트레이션** | `application/` |
| 여러 Aggregate 협력이 반복되는 로직 | **Domain Service** | `domain/` (I/O 없음) 또는 `application/` (I/O 포함) |
| 어쩔 수 없이 이벤트 유지 + 같은 tx | `@TransactionalEventListener(BEFORE_COMMIT)` | **`application/`** (adapter-in 금지) |
| Eventual consistency OK | `AFTER_COMMIT` + 이벤트 구독자 | `application/` (in-process) / `infrastructure/adapter/in/messaging/` (broker) |

## 왜 "이벤트 + same-tx"가 나쁜가

같은 tx로 엮는 순간 이벤트의 장점이 대부분 사라진다.

| 이벤트의 장점 | same-tx 엮었을 때 |
|------------|----------------|
| 느슨한 결합 | tx 실패하면 publisher도 함께 롤백 → 런타임 결합 강함 |
| 관찰가능성 / 대체가능성 | 동기 인라인 호출과 동일, 추가 이익 없음 |
| 확장성 (브로커 전환) | tx를 엮은 순간 브로커로 전환 불가 |
| 명시적 트랜잭션 경계 | 이벤트 발행자가 tx를 모르게 설계되는데 실은 엮임 → 추론 어려움 |

**결론**: 이벤트 + same-tx는 "직접 호출의 복잡한 우회"가 된다. 그럴 바엔 Application Service에서 명시적으로 호출하는 것이 읽기·디버깅·테스트 모두 편하다.

## 결정 트리

```
이 컨슈머가 publisher의 tx에 참여해야 하는가?
├─ No (eventual OK)
│    ├─ in-process     → application/
│    └─ external broker → infrastructure/adapter/in/messaging/
└─ Yes (atomic 필요)
     └─ 이벤트 쓰지 말고 Application Service에서 직접 오케스트레이션
        (부득이 이벤트 유지 시 application/ 에만, adapter-in 절대 금지)
```

## Spring Transaction Phase 가이드

`@TransactionalEventListener`의 `phase` 선택:

| Phase | 언제 실행 | 적합한 케이스 | 부적합한 케이스 |
|-------|----------|-------------|--------------|
| `AFTER_COMMIT` (기본) | 커밋 직후 | 외부 I/O, 알림, 후속 eventual 처리 | 실패 시 롤백 원하는 경우 |
| `BEFORE_COMMIT` | 커밋 직전 | 같은 tx 내 원자 정리 (탈퇴 등) | 대부분 — 사용 시 설계 재검토 |
| `AFTER_ROLLBACK` | 롤백 직후 | 보상 로직, 실패 알림 | 로직 수행 |
| `AFTER_COMPLETION` | 커밋/롤백 무관 완료 후 | 리소스 해제 | 비즈니스 로직 |

`@Async` + `REQUIRES_NEW` 조합 = 외부 I/O 비동기 처리 기본값.

## lockr-server 적용 사례

### 🚩 리팩터 대상: `WithdrawnUserEventConsumer`

현재 구조:
```java
// auth/signin/api/WithdrawnUserEventConsumer
@TransactionalEventListener(phase = BEFORE_COMMIT)
public void consume(final WithdrawnUserEvent event) {
    oidcRepository.deleteByUserId(event.userId());
    signInTokenRepository.deleteByUserId(event.userId());
}
```

**문제**:
- `BEFORE_COMMIT` = publisher tx에 참여 (same-tx)
- 이벤트 + same-tx = 앞서 설명한 안티패턴
- 위치도 `api/` — 오케스트레이션은 `application/`이 맞음

**권장 리팩터**: users의 `WithdrawUsersService`가 auth cleanup을 직접 호출.

```java
// application/ (users)
class WithdrawUsersService implements WithdrawUsersUseCase {
    public void withdraw(WithdrawCommand cmd) {
        users.withdraw(cmd.userId());
        authCleanupService.cleanup(cmd.userId());  // 같은 tx, 명시적
    }
}
```

**효과**:
- tx 경계가 Application에 명확
- 이벤트는 진짜 eventual용으로만 남음
- `api/` 리스너 제거

### ✅ 정합: in-process 오케스트레이션

- `FeedEventConsumer`가 `CreatedScheduleEvent` 받아서 Feed 생성 (`AFTER_COMMIT`)
- `ClubEventConsumer`가 `ApprovedApplicationEvent` 받아서 멤버 추가 (`AFTER_COMMIT`)

→ eventual OK, tx 별개. 위치만 `api/` → `application/` 이동 권장.

### ✅ 정합: 외부 I/O

- `FeeNotificationEventConsumer` (FCM)
- `ScheduleNotificationEventConsumer` (FCM)
- `ChatEventListener` (SSE)

→ `infrastructure/`에 둔 것 정합. 단 유실 리스크는 [Outbox 문서](03-outbox-consideration.md) 참고.

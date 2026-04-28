# ADR-0008: IdempotentEventHandler — ApplicationListener 진입점 흡수

- **상태**: 승인됨
- **날짜**: 2026-04-28
- **관련 레이어**: global/inbox

## 컨텍스트

hotfix `1c43d3b`에서 `FeeNotificationEventConsumer`에 `@EventListener` + `@Transactional` + `handle()` 호출 패턴을 임시 적용했다. 이는 `IdempotentEventHandler.handle()`에 붙은 `@Transactional`이 self-invocation으로 무력화되는 문제의 workaround였다.

- 자식 Consumer가 `@EventListener onEvent()` → `this.handle()` 순으로 호출하면, `handle()`은 Spring AOP proxy가 아닌 `this`(raw object)를 통해 호출되므로 `@Transactional`이 작동하지 않는다.
- 결과적으로 `JOOQInboxRepository.insertIfAbsent`의 `Propagation.MANDATORY`가 "활성 트랜잭션 없음" 예외를 던진다.
- hotfix는 자식이 outer transaction을 직접 시작하도록 강제했으나, 이 패턴이 새 Consumer마다 반복될 경우 누락 위험이 있다.

이 임시 해결책을 제거하고 베이스 클래스가 진입점을 영구적으로 소유하도록 아키텍처적으로 수정한다.

## 고려한 선택지

### 옵션 A: `ApplicationListener<PayloadApplicationEvent<E>>` 구현 (채택)

`IdempotentEventHandler`가 `ApplicationListener<PayloadApplicationEvent<E>>`를 구현하고
`onApplicationEvent()`에서 `@Transactional`을 소유한다.

Spring의 `SimpleApplicationEventMulticaster`는 `ApplicationEvent`를 상속하지 않는 객체를
`publishEvent(Object)` 호출 시 자동으로 `PayloadApplicationEvent<T>`로 래핑한다(Spring 4.2+ 표준).
`IntegrationDomainEvent`는 `ApplicationEvent`를 상속하지 않으므로 이 래핑이 항상 발생한다.

**장점:**
- `onApplicationEvent()`는 Spring이 proxy를 통해 호출하므로 `@Transactional`이 정상 동작
- 자식 Consumer는 `consumerName()` + `doHandle()` + 생성자만 구현하면 됨 (boilerplate 0)
- self-invocation 함정 자체가 사라짐

**단점:**
- `PayloadApplicationEvent` 래핑 동작에 대한 Spring 내부 지식 필요

### 옵션 B: `@EventListener` abstract 메서드 (기각)

abstract 베이스 클래스에 `@EventListener`를 붙인 concrete 메서드를 두고, 자식이 상속.

**기각 근거:**
- `GenericTypeAwareEventListenerFactory`가 abstract class의 메서드 레벨 어노테이션을 처리할 때
  자식별 `ResolvableType` 추론이 불안정할 수 있다.
- Spring 공식 문서에서 이 패턴을 명시적으로 보장하지 않는다.

## 결정

**옵션 A를 채택한다.**

`IdempotentEventHandler`가 `ApplicationListener<PayloadApplicationEvent<E>>`를 구현하고,
`onApplicationEvent()`를 `final @Transactional` 메서드로 선언한다.
`handle()`은 `protected`로 낮춰 외부 직접 호출을 봉인한다.

```java
public abstract class IdempotentEventHandler<E extends IntegrationDomainEvent>
        implements ApplicationListener<PayloadApplicationEvent<E>> {

    @Transactional
    @Override
    public void onApplicationEvent(PayloadApplicationEvent<E> wrapper) {
        // ⚠ override 금지 — Spring CGLIB 프록시는 final 메서드를 가공할 수 없어
        // final을 붙이지 못한다. 자식이 override하면 @Transactional 무력화 + self-invocation 재발.
        handle(wrapper.getPayload());
    }

    protected void handle(E event) {
        if (!inbox.insertIfAbsent(event.eventId(), consumerName())) return;
        doHandle(event);
    }
}
```

Consumer 구현체는 다음만 포함한다:

```java
@Component
public class FeeNotificationEventConsumer extends IdempotentEventHandler<UnpaidFeeNotifiedEvent> {

    public FeeNotificationEventConsumer(InboxRepository inbox, ...) { super(inbox); ... }

    @Override protected String consumerName() { return "fee.unpaid_notification"; }

    @Override protected void doHandle(UnpaidFeeNotifiedEvent event) { ... }
}
```

## 근거

- `PayloadApplicationEvent` 자동 래핑은 Spring 4.2+ `AbstractApplicationEventMulticaster`의 표준 동작이며 Spring Boot 3.x에서 안정적으로 보장된다.
- `ApplicationListener` generic 타입 추론은 `ResolvableType.forClass(beanClass)`를 통해 자식 구체 타입에서 정확히 결정된다. Spring 공식 문서 "Generic Events" 섹션에서 표준 패턴으로 명시.
- `onApplicationEvent()`는 Spring AOP proxy를 통해 호출되므로 `@Transactional`이 self-invocation 없이 정상 동작한다.
- 새 Consumer 구현 시 boilerplate가 0이므로 누락 위험이 구조적으로 제거된다.

## 트랜잭션 위치 — In-adapter 선택 근거 (헥사고날 컨벤션 분기)

본 ADR은 **In-adapter (Consumer)**가 `@Transactional`을 소유하는 결정이다.
이는 우리 프로젝트 Command 흐름의 *"Out-adapter (Repository.save)만 트랜잭션 소유"* 컨벤션과
의도적으로 다른 갈래이며, **Event 흐름의 별도 컨벤션**으로 명시한다.

### 헥사고날 어댑터 분류 정리

- **In-adapter (Driving)**: 외부 → 도메인. HTTP Controller, EventListener, Kafka Listener 등
- **Out-adapter (Driven)**: 도메인 → 외부. JOOQ Repository, HttpClient, FCM 발송기 등
- `infrastructure/` 패키지는 두 종류 모두 담음. 분류는 패키지가 아닌 **트래픽 방향**.

### 두 흐름의 트랜잭션 컨벤션 분기

| 흐름 | 진입점 | 트랜잭션 소유 | 근거 |
|------|--------|------------|------|
| **Command** (HTTP/CLI) | Api Controller → UseCase → Service(POJO) | Out-adapter (`JOOQ*Repository.save`) | 단일 Aggregate save = 단일 트랜잭션. Service POJO 유지 |
| **Event** (Spring/Kafka 이벤트) | `IdempotentEventHandler` (In-adapter) | **In-adapter (`@Transactional` on `onApplicationEvent`)** | inbox 멱등 가드와 doHandle 비즈니스 로직의 원자성 보장 |

### Event 흐름이 In-adapter에 트랜잭션을 두는 이유

검토한 대안과 기각 근거:

1. **Service에 `@Transactional`** — 헥사고날 정설이지만 *"Service POJO"* 컨벤션 위배.
2. **Out-adapter (Repository.save)만 트랜잭션** — `inbox.insertIfAbsent`와 비즈니스 `Repository.save`가
   각자 다른 트랜잭션이 되어 inbox 가드 의도 깨짐 (doHandle 실패 시 inbox row만 commit → 이벤트 영구 유실).
3. **In-adapter 트랜잭션 (채택)** — Service POJO 컨벤션 유지하면서 inbox + doHandle 원자성 보장.

### 진화 경로 (다음 단계)

본 ADR은 *"트랜잭션 위치"*를 In-adapter로 확정하지만, **doHandle 안의 비즈니스 로직은 Service(POJO)로 추출**하는 것을 권장한다:

```java
// In-adapter: 트랜잭션 + 멱등 가드 (이번 ADR로 확정)
public abstract class IdempotentEventHandler<E> { ... }

// 자식: Service에 위임만 (다음 진화 단계)
@Component
public class FeeNotificationEventConsumer extends IdempotentEventHandler<UnpaidFeeNotifiedEvent> {
    private final HandleUnpaidFeeNotifiedUseCase useCase;
    @Override protected void doHandle(UnpaidFeeNotifiedEvent event) {
        useCase.handle(event);  // ← Service 위임
    }
}

// Service (POJO, @Transactional X)
@Service
public class FeeNotificationEventService implements HandleUnpaidFeeNotifiedUseCase {
    public void handle(UnpaidFeeNotifiedEvent event) {
        // 비즈니스 로직만
    }
}
```

이 분리는 (a) 비즈니스 책임 격리, (b) Kafka 전환 시 In-adapter만 교체하면 되는 헥사고날 가치를 보존한다.
다음 IntegrationDomainEvent Consumer 도입 시점에 자연스럽게 적용한다.

## 결과

- `FeeNotificationEventConsumer`에서 `onEvent()`, `@EventListener`, `@Transactional` 완전 제거.
- 새 `IntegrationDomainEvent` Consumer는 `consumerName()` + `doHandle()` + 생성자만 구현하면 이벤트를 자동 수신한다.
- `JOOQInboxRepository.insertIfAbsent(Propagation.MANDATORY)`는 `onApplicationEvent()`가 시작한 트랜잭션에 정상 join한다.
- 통합 테스트(`IdempotentEventHandlerIntegrationTest`)로 self-invocation 회귀 방지 및 트랜잭션 경계 검증.

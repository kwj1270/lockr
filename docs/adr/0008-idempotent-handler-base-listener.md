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

## 결과

- `FeeNotificationEventConsumer`에서 `onEvent()`, `@EventListener`, `@Transactional` 완전 제거.
- 새 `IntegrationDomainEvent` Consumer는 `consumerName()` + `doHandle()` + 생성자만 구현하면 이벤트를 자동 수신한다.
- `JOOQInboxRepository.insertIfAbsent(Propagation.MANDATORY)`는 `onApplicationEvent()`가 시작한 트랜잭션에 정상 join한다.
- 통합 테스트(`IdempotentEventHandlerIntegrationTest`)로 self-invocation 회귀 방지 및 트랜잭션 경계 검증.

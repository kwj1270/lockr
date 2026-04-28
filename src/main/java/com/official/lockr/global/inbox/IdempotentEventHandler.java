package com.official.lockr.global.inbox;

import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;

/**
 * Outbox 이벤트 컨슈머 베이스. inbox 기반 멱등성 방어.
 *
 * <p>이 클래스는 {@link ApplicationListener}{@code <PayloadApplicationEvent<E>>}를 구현하므로
 * {@code applicationEventPublisher.publishEvent(event)} 호출 시 Spring이 자동으로
 * {@link PayloadApplicationEvent}{@code <E>}로 래핑하여 {@code onApplicationEvent()}에 전달한다.
 *
 * <p>{@code onApplicationEvent()}가 진입점이다.
 * {@code @Transactional}은 {@link IdempotentExecutor} 구현체에 위치하므로 이 베이스는 도구를 알 필요가 없다.
 * {@code doHandle()}이 예외를 던지면 {@code IdempotentExecutor} 구현체가 가드를 무효화하여
 * 재전달된 이벤트가 skip으로 유실되지 않는다.
 *
 * <p>구현체는 {@code consumerName()} + {@code doHandle()} + 생성자만 구현하면 된다.
 * {@code @EventListener} / {@code @Transactional} boilerplate 불필요 — 베이스가 직접 소유.
 *
 * <p><b>⚠ {@code onApplicationEvent()} override 금지</b> — Spring CGLIB 프록시는 final 메서드를
 * 가공할 수 없어 {@code final}을 붙일 수 없다. 자식이 override하면 {@code IdempotentExecutor}의
 * AOP 트랜잭션이 무력화되고 self-invocation 함정이 재발할 수 있다.
 */
public abstract class IdempotentEventHandler<E extends IntegrationDomainEvent>
        implements ApplicationListener<PayloadApplicationEvent<E>> {

    private final IdempotentExecutor executor;

    protected IdempotentEventHandler(final IdempotentExecutor executor) {
        this.executor = executor;
    }

    protected abstract String consumerName();

    protected abstract void doHandle(E event);

    @Override
    public void onApplicationEvent(final PayloadApplicationEvent<E> wrapper) {
        final E event = wrapper.getPayload();
        executor.executeIfAbsent(event.eventId(), consumerName(), () -> doHandle(event));
    }
}

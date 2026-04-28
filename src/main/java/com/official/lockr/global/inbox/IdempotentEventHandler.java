package com.official.lockr.global.inbox;

import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox 이벤트 컨슈머 베이스. inbox 기반 멱등성 방어.
 *
 * <p>이 클래스는 {@link ApplicationListener}{@code <PayloadApplicationEvent<E>>}를 구현하므로
 * {@code applicationEventPublisher.publishEvent(event)} 호출 시 Spring이 자동으로
 * {@link PayloadApplicationEvent}{@code <E>}로 래핑하여 {@code onApplicationEvent()}에 전달한다.
 *
 * <p>{@code onApplicationEvent()}가 진입점이며 {@code @Transactional}을 소유한다.
 * {@code doHandle()}이 예외를 던지면 inbox INSERT도 함께 롤백되므로 재전달된 이벤트가 skip으로 유실되지 않는다.
 *
 * <p>구현체는 {@code consumerName()} + {@code doHandle()} + 생성자만 구현하면 된다.
 * {@code @EventListener} / {@code @Transactional} boilerplate 불필요 — 베이스가 직접 소유.
 *
 * <p><b>⚠ {@code onApplicationEvent()} override 금지</b> — Spring CGLIB 프록시는 final 메서드를
 * 가공할 수 없어 {@code final}을 붙일 수 없다. 자식이 override하면 {@code @Transactional}이
 * 무력화되고 self-invocation 함정이 재발할 수 있다.
 */
public abstract class IdempotentEventHandler<E extends IntegrationDomainEvent>
        implements ApplicationListener<PayloadApplicationEvent<E>> {

    private final InboxRepository inbox;

    protected IdempotentEventHandler(final InboxRepository inbox) {
        this.inbox = inbox;
    }

    protected abstract String consumerName();

    protected abstract void doHandle(E event);

    @Transactional
    @Override
    public void onApplicationEvent(final PayloadApplicationEvent<E> wrapper) {
        handle(wrapper.getPayload());
    }

    protected void handle(final E event) {
        if (!inbox.insertIfAbsent(event.eventId(), consumerName())) {
            return;
        }
        doHandle(event);
    }
}

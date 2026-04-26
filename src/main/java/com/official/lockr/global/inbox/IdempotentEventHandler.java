package com.official.lockr.global.inbox;

import com.official.lockr.global.ddd.IntegrationDomainEvent;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox 이벤트 컨슈머 베이스. inbox 기반 멱등성 방어.
 *
 * <p>{@code handle()}은 단일 트랜잭션으로 묶여 있다. {@code doHandle()}이 예외를 던지면
 * inbox INSERT도 함께 롤백되므로 재전달된 이벤트가 skip으로 유실되지 않는다.
 * 구현체는 Spring Bean(@Component 등)으로 등록해야 AOP proxy가 걸려 @Transactional이 동작한다.
 * self-invocation({@code this.handle()})로 호출하지 말 것 — 프록시 우회로 트랜잭션이 무력화된다.
 */
public abstract class IdempotentEventHandler<E extends IntegrationDomainEvent> {

    private final InboxRepository inbox;

    protected IdempotentEventHandler(final InboxRepository inbox) {
        this.inbox = inbox;
    }

    protected abstract String consumerName();

    protected abstract void doHandle(E event);

    @Transactional
    public void handle(final E event) {
        if (!inbox.insertIfAbsent(event.eventId(), consumerName())) {
            return;
        }
        doHandle(event);
    }
}

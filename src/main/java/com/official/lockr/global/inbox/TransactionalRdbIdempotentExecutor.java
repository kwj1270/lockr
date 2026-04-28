package com.official.lockr.global.inbox;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * RDB(inbox 테이블) 기반 멱등 실행 구현체.
 *
 * <p>{@code @Transactional}을 이 클래스가 소유하여 inbox INSERT와 {@code doHandle} 비즈니스 로직을
 * 하나의 트랜잭션으로 묶는다. {@code doHandle} 실패 시 트랜잭션 롤백으로 inbox 행도 함께 제거되어
 * 이벤트 재처리가 가능하다.
 *
 * <p>{@link JOOQInboxRepository#insertIfAbsent}는 {@code Propagation.MANDATORY}이므로
 * 이 메서드의 {@code @Transactional}이 반드시 외부 트랜잭션을 시작해야 한다.
 */
@Component
public class TransactionalRdbIdempotentExecutor implements IdempotentExecutor {

    private final InboxRepository inbox;

    public TransactionalRdbIdempotentExecutor(final InboxRepository inbox) {
        this.inbox = inbox;
    }

    @Transactional
    @Override
    public void executeIfAbsent(
            final String eventId,
            final String consumerName,
            final Runnable doHandle
    ) {
        if (!inbox.insertIfAbsent(eventId, consumerName)) {
            return;
        }
        doHandle.run();
    }
}

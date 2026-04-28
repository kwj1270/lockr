package com.official.lockr.global.inbox;

import java.util.HashSet;
import java.util.Set;

/**
 * 테스트용 {@link IdempotentExecutor} 구현체.
 *
 * <p>{@code HashSet} 기반으로 {@code (eventId:consumerName)} 키를 관리한다.
 * {@code @Transactional} 없음. {@code doHandle} 예외 시 키를 수동 제거하여
 * {@code TransactionalRdbIdempotentExecutor}의 트랜잭션 롤백과 동일한 의미를 재현한다.
 *
 * <p>테스트 간 격리를 위해 {@code @BeforeEach}에서 {@code clear()}를 호출한다.
 */
public class InMemoryIdempotentExecutor implements IdempotentExecutor {

    private final Set<String> consumed = new HashSet<>();

    @Override
    public void executeIfAbsent(final String eventId, final String consumerName, final Runnable doHandle) {
        final String key = eventId + ":" + consumerName;
        if (!consumed.add(key)) {
            return;
        }
        try {
            doHandle.run();
        } catch (RuntimeException e) {
            consumed.remove(key);  // RDB 롤백과 의미 일치 — 예외 시 가드 제거
            throw e;
        }
    }

    public void clear() {
        consumed.clear();
    }
}

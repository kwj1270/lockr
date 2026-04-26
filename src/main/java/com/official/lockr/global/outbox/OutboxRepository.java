package com.official.lockr.global.outbox;

import java.util.List;

public interface OutboxRepository {
    int MAX_RETRY = 10;

    void insert(OutboxEntry entry);
    List<OutboxEntry> findUnprocessed(int limit);
    void markProcessed(String eventId);
    void incrementRetry(String eventId);

    /**
     * retry 상한에 도달해 더 이상 폴링되지 않는 stuck entry 수.
     * 운영 모니터링/알람 용도이며 0보다 크면 즉시 조사가 필요하다.
     */
    long countStuck();
}

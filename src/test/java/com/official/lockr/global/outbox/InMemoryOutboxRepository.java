package com.official.lockr.global.outbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InMemoryOutboxRepository implements OutboxRepository {

    private final Map<String, OutboxEntry> store = new HashMap<>();
    private final List<OutboxEntry> insertionOrder = new ArrayList<>();
    private final Set<String> processedIds = new HashSet<>();
    private final Map<String, Integer> retryCounts = new HashMap<>();

    @Override
    public void insert(final OutboxEntry entry) {
        store.put(entry.eventId(), entry);
        insertionOrder.add(entry);
    }

    @Override
    public List<OutboxEntry> findUnprocessed(final int limit) {
        return insertionOrder.stream()
                .filter(e -> !processedIds.contains(e.eventId()))
                .filter(e -> getRetryCount(e.eventId()) < MAX_RETRY)
                .limit(limit)
                .toList();
    }

    @Override
    public void markProcessed(final String eventId) {
        processedIds.add(eventId);
    }

    @Override
    public void incrementRetry(final String eventId) {
        retryCounts.merge(eventId, 1, Integer::sum);
    }

    @Override
    public long countStuck() {
        return insertionOrder.stream()
                .filter(e -> !processedIds.contains(e.eventId()))
                .filter(e -> getRetryCount(e.eventId()) >= MAX_RETRY)
                .count();
    }

    public List<OutboxEntry> findAll() {
        return new ArrayList<>(insertionOrder);
    }

    public void clear() {
        store.clear();
        insertionOrder.clear();
        processedIds.clear();
        retryCounts.clear();
    }

    public boolean isProcessed(final String eventId) {
        return processedIds.contains(eventId);
    }

    public int getRetryCount(final String eventId) {
        return retryCounts.getOrDefault(eventId, 0);
    }
}

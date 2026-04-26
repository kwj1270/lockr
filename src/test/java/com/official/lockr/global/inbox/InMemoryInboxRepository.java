package com.official.lockr.global.inbox;

import java.util.HashSet;
import java.util.Set;

public class InMemoryInboxRepository implements InboxRepository {

    private final Set<String> consumed = new HashSet<>();

    @Override
    public boolean insertIfAbsent(final String eventId, final String consumerName) {
        final String key = eventId + ":" + consumerName;
        return consumed.add(key);
    }

    public void clear() {
        consumed.clear();
    }
}

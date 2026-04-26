package com.official.lockr.global.inbox;

public interface InboxRepository {
    boolean insertIfAbsent(String eventId, String consumerName);
}

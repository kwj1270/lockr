package com.official.lockr.global.outbox;

import java.time.LocalDateTime;

public record OutboxEntry(CloudEventEnvelope envelope) {

    public String eventId() {
        return envelope.id();
    }

    public String eventType() {
        return envelope.type();
    }

    public String source() {
        return envelope.source();
    }

    public String aggregateId() {
        return envelope.subject();
    }

    public LocalDateTime occurredAt() {
        return envelope.time();
    }
}

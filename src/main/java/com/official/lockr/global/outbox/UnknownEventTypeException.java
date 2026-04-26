package com.official.lockr.global.outbox;

public class UnknownEventTypeException extends RuntimeException {
    public UnknownEventTypeException(final String eventType) {
        super("Unknown event type: " + eventType);
    }
}

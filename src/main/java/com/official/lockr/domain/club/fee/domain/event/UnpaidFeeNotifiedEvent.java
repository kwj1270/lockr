package com.official.lockr.domain.club.fee.domain.event;

import com.official.lockr.global.ddd.IntegrationDomainEvent;

import java.time.LocalDateTime;
import java.util.List;

public record UnpaidFeeNotifiedEvent(
        String eventId,
        String aggregateId,
        String clubId,
        int year,
        int month,
        String sentBy,
        List<String> memberIds,
        LocalDateTime occurredAt
) implements IntegrationDomainEvent {

    private static final String EVENT_TYPE = "com.official.lockr.club.fee.unpaid_notified";
    private static final String SOURCE = "lockr://club/fee";

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public String source() {
        return SOURCE;
    }
}

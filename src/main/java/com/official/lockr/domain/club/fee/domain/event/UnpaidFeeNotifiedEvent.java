package com.official.lockr.domain.club.fee.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;

public record UnpaidFeeNotifiedEvent(
        String clubId,
        int year,
        int month,
        String sentBy,
        List<String> memberIds,
        LocalDateTime occurredAt
) implements DomainEvent {
}

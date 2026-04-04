package com.official.lockr.domain.club.fee.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record FeePolicyChangedEvent(
        String policyId,
        String clubId,
        int amount,
        int dueDay,
        LocalDateTime changedAt
) implements DomainEvent {
}

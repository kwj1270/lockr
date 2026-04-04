package com.official.lockr.domain.club.fee.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.util.List;

public record UnpaidFeeNotifiedEvent(
        String clubId,
        int year,
        int month,
        List<String> notifiedMemberIds
) implements DomainEvent {
}

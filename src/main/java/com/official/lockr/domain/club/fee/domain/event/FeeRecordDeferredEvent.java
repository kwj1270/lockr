package com.official.lockr.domain.club.fee.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record FeeRecordDeferredEvent(
        String recordId,
        String clubId,
        String memberId,
        int year,
        int month
) implements DomainEvent {
}

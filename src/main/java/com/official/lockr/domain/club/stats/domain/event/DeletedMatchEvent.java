package com.official.lockr.domain.club.stats.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record DeletedMatchEvent(
        String matchRecordId,
        String clubId,
        String season
) implements DomainEvent {
}

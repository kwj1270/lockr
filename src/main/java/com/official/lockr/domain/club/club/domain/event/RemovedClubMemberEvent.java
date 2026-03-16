package com.official.lockr.domain.club.club.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record RemovedClubMemberEvent(
        String clubId,
        String userId
) implements DomainEvent {
}

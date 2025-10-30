package com.official.lockr.domain.club.match.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record AcceptedMatchEvent(
        String matchProposeId,
        String homeClubId,
        String awayClubId
) implements DomainEvent {
}

package com.official.lockr.domain.club.match.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record AcceptedMatchEvent(
        String matchProposeId,
        String homeClubId,
        String homeClubMangerUserId,
        String awayClubId,
        String awayClubMangerUserId,
        String location,
        LocalDateTime matchDateTime
) implements DomainEvent {
}

package com.official.lockr.domain.club.match.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record InviteMatchEvent(
        String matchId,
        String homeClubId,
        String awayClubId,
        LocalDateTime matchDateTime,
        String location
) implements DomainEvent {
}

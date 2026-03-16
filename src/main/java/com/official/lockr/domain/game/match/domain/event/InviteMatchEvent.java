package com.official.lockr.domain.game.match.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record InviteMatchEvent(
        String matchId,
        String homeTeamId,
        String awayTeamId,
        LocalDateTime matchDateTime,
        String location
) implements DomainEvent {
}

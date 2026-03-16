package com.official.lockr.domain.game.game.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record FinishedGameEvent(
        String gameId,
        int homeScore,
        int awayScore,
        int totalMinutes
) implements DomainEvent {
}

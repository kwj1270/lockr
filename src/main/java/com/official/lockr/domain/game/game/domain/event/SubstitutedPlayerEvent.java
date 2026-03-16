package com.official.lockr.domain.game.game.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record SubstitutedPlayerEvent(
        String gameId,
        String teamId,
        String outPlayerId,
        String inPlayerId,
        int minute
) implements DomainEvent {
}

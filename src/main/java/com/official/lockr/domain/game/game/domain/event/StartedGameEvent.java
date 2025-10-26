package com.official.lockr.domain.game.game.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record StartedGameEvent(
        String gameId
) implements DomainEvent {
}

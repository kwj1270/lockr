package com.official.lockr.domain.game.match.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record AcceptedMatchEvent(
        String matchProposeId,
        String homeTeamId,
        String awayTeamId
) implements DomainEvent {
}

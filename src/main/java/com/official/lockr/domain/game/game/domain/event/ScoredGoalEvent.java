package com.official.lockr.domain.game.game.domain.event;

import com.official.lockr.domain.relay.domain.RelayEventType;
import com.official.lockr.domain.game.common.Score;
import com.official.lockr.global.ddd.DomainEvent;
import jakarta.annotation.Nullable;

public record ScoredGoalEvent(
        String id,
        String teamId,
        RelayEventType eventType,
        String playerId,
        @Nullable String relatedPlayerId,
        int minute,
        Score homeScore,
        Score awayScore
) implements DomainEvent {
}

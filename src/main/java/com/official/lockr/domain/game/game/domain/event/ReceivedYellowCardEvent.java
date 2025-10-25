package com.official.lockr.domain.game.game.domain.event;

import com.official.lockr.domain.relay.domain.RelayEventType;
import com.official.lockr.global.ddd.DomainEvent;

public record ReceivedYellowCardEvent(
        String id,
        String teamId,
        RelayEventType relayEventType,
        String playerId,
        int minute
) implements DomainEvent {
}

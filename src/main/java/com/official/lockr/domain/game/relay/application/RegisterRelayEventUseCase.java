package com.official.lockr.domain.game.relay.application;

import com.official.lockr.domain.game.relay.domain.RelayEvent;

public interface RegisterRelayEventUseCase {
    RelayEvent append(final RelayEvent event);
}

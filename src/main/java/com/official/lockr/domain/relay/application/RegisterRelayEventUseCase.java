package com.official.lockr.domain.relay.application;

import com.official.lockr.domain.relay.domain.RelayEvent;

public interface RegisterRelayEventUseCase {
    RelayEvent append(final RelayEvent event);
}

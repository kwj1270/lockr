package com.official.lockr.domain.relay.domain;

public interface RelayEventRepository {
    RelayEvent save(final RelayEvent event);
}

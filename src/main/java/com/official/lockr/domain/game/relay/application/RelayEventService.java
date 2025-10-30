package com.official.lockr.domain.game.relay.application;

import com.official.lockr.domain.game.relay.domain.RelayEvent;
import com.official.lockr.domain.game.relay.domain.RelayEventRepository;
import org.springframework.stereotype.Service;

@Service
public class RelayEventService implements RegisterRelayEventUseCase {

    private final RelayEventRepository relayEventRepository;

    public RelayEventService(final RelayEventRepository relayEventRepository) {
        this.relayEventRepository = relayEventRepository;
    }

    @Override
    public RelayEvent append(final RelayEvent event) {
        return relayEventRepository.save(event);
    }
}

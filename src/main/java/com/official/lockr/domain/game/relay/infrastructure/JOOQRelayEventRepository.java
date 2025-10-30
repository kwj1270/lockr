package com.official.lockr.domain.game.relay.infrastructure;

import com.official.lockr.domain.game.relay.domain.RelayEvent;
import com.official.lockr.domain.game.relay.domain.RelayEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JOOQRelayEventRepository implements RelayEventRepository {

    @Transactional
    @Override
    public RelayEvent save(final RelayEvent event) {
        return null;
    }
}

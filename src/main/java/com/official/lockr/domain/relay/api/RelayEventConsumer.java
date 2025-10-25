package com.official.lockr.domain.relay.api;

import com.official.lockr.domain.game.game.domain.event.FinishedGameEvent;
import com.official.lockr.domain.game.game.domain.event.StartedGameEvent;
import com.official.lockr.domain.relay.application.RegisterRelayEventUseCase;
import com.official.lockr.domain.relay.domain.RelayEvent;
import com.official.lockr.global.util.UlidUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RelayEventConsumer {

    private final RegisterRelayEventUseCase registerRelayEventUseCase;

    public RelayEventConsumer(final RegisterRelayEventUseCase registerRelayEventUseCase) {
        this.registerRelayEventUseCase = registerRelayEventUseCase;
    }

    @EventListener
    public void consume(final StartedGameEvent event) {
        final RelayEvent startEvent = RelayEvent.start(UlidUtils.generateUlid(), event.gameId());
        registerRelayEventUseCase.append(startEvent);
    }

    @EventListener
    public void consume(final FinishedGameEvent event) {
        final RelayEvent finishEvent = RelayEvent.finish(UlidUtils.generateUlid(), event.gameId(), event.totalMinutes(), event.homeScore(), event.awayScore());
        registerRelayEventUseCase.append(finishEvent);
    }
}

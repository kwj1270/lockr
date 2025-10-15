package com.official.lockr.domain.club.squard.api;

import com.official.lockr.domain.club.squard.application.AddPlayerUseCase;
import com.official.lockr.domain.club.squard.application.dto.AddPlayerCommand;
import com.official.lockr.domain.club.team.domain.event.AddedMemberEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SquadEventConsumer {

    private final AddPlayerUseCase addPlayerUseCase;

    public SquadEventConsumer(final AddPlayerUseCase addPlayerUseCase) {
        this.addPlayerUseCase = addPlayerUseCase;
    }

    @EventListener
    public void create(final AddedMemberEvent event) {
        addPlayerUseCase.addPlayer(new AddPlayerCommand(event.teamId(), event.userId(), event.id()));
    }
}

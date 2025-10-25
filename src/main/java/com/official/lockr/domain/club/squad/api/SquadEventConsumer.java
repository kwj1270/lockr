package com.official.lockr.domain.club.squad.api;

import com.official.lockr.domain.club.squad.application.usecase.AddSquadPlayerUseCase;
import com.official.lockr.domain.club.squad.application.dto.AddSquadPlayerCommand;
import com.official.lockr.domain.club.team.domain.event.AddedMemberEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SquadEventConsumer {

    private final AddSquadPlayerUseCase addSquadPlayerUseCase;

    public SquadEventConsumer(final AddSquadPlayerUseCase addSquadPlayerUseCase) {
        this.addSquadPlayerUseCase = addSquadPlayerUseCase;
    }

    @EventListener
    public void create(final AddedMemberEvent event) {
        addSquadPlayerUseCase.addSquadPlayer(new AddSquadPlayerCommand(event.teamId(), event.userId(), event.id()));
    }
}

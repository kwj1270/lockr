package com.official.lockr.domain.club.sqaud.api;

import com.official.lockr.domain.club.sqaud.application.usecase.AddSquadPlayerUseCase;
import com.official.lockr.domain.club.sqaud.application.dto.AddSquadPlayerCommand;
import com.official.lockr.domain.club.club.domain.event.AddedMemberEvent;
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
        addSquadPlayerUseCase.addSquadPlayer(new AddSquadPlayerCommand(event.clubId(), event.userId(), event.id()));
    }
}

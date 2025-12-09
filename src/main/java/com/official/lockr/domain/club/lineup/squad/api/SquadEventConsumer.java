package com.official.lockr.domain.club.lineup.squad.api;

import com.official.lockr.domain.club.club.domain.event.AddedMemberEvent;
import com.official.lockr.domain.club.lineup.squad.application.dto.AddPlayerCommand;
import com.official.lockr.domain.club.lineup.squad.application.usecase.AddPlayerUseCase;
import com.official.lockr.domain.club.recruitment.applications.domain.event.ApprovedApplicationEvent;
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
        addPlayerUseCase.addPlayer(new AddPlayerCommand(event.clubId(), event.userId()));
    }

    @EventListener
    public void create(final ApprovedApplicationEvent event) {
        addPlayerUseCase.addPlayer(new AddPlayerCommand(event.clubId(), event.userId()));
    }
}

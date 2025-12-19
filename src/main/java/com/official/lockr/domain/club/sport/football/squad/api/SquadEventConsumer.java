package com.official.lockr.domain.club.sport.football.squad.api;

import com.official.lockr.domain.club.club.domain.event.AddedClubMemberEvent;
import com.official.lockr.domain.club.sport.football.squad.application.dto.AddFootBallPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.AddSquadPlayerUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SquadEventConsumer {

    private final AddSquadPlayerUseCase addSquadPlayerUseCase;

    public SquadEventConsumer(final AddSquadPlayerUseCase addSquadPlayerUseCase) {
        this.addSquadPlayerUseCase = addSquadPlayerUseCase;
    }

    @EventListener
    public void create(final AddedClubMemberEvent event) {
        if (event.sportType().equals("FOOT_BALL")) {
            addSquadPlayerUseCase.addPlayer(new AddFootBallPlayerCommand(event.clubId(), event.userId()));
        }
    }

//    @TransactionalEventListener
//    public void create(final ApprovedApplicationEvent event) {
//        if(event.sportType().equals("FOOT_BALL")) {
//            addFootBallPlayerUseCase.addPlayer(new AddFootBallPlayerCommand(event.clubId(), event.userId()));
//        }
//    }
}

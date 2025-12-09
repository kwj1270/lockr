package com.official.lockr.domain.club.lineup.squad.api;

import com.official.lockr.domain.club.club.domain.event.AddedClubMemberEvent;
import com.official.lockr.domain.club.lineup.squad.application.dto.AddFootBallPlayerCommand;
import com.official.lockr.domain.club.lineup.squad.application.usecase.AddFootBallPlayerUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SquadEventConsumer {

    private final AddFootBallPlayerUseCase addFootBallPlayerUseCase;

    public SquadEventConsumer(final AddFootBallPlayerUseCase addFootBallPlayerUseCase) {
        this.addFootBallPlayerUseCase = addFootBallPlayerUseCase;
    }

    @TransactionalEventListener
    public void create(final AddedClubMemberEvent event) {
        if (event.sportType().equals("FOOT_BALL")) {
            addFootBallPlayerUseCase.addPlayer(new AddFootBallPlayerCommand(event.clubId(), event.userId()));
        }
    }

//    @TransactionalEventListener
//    public void create(final ApprovedApplicationEvent event) {
//        if(event.sportType().equals("FOOT_BALL")) {
//            addFootBallPlayerUseCase.addPlayer(new AddFootBallPlayerCommand(event.clubId(), event.userId()));
//        }
//    }
}

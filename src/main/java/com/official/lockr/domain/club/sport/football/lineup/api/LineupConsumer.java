package com.official.lockr.domain.club.sport.football.lineup.api;

import com.official.lockr.domain.club.club.domain.event.FoundClubEvent;
import com.official.lockr.domain.club.sport.football.lineup.application.command.AddLineupsCommand;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.AddLineupUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LineupConsumer {

    private final AddLineupUseCase addLineupUseCase;

    public LineupConsumer(final AddLineupUseCase addLineupUseCase) {
        this.addLineupUseCase = addLineupUseCase;
    }

    @TransactionalEventListener
    public void consume(final FoundClubEvent event) {
        if (event.sportType().equals("FOOT_BALL")) {
            addLineupUseCase.addAll(new AddLineupsCommand(event.id()));
        }
    }
}

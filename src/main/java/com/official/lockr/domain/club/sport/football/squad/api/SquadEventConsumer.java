package com.official.lockr.domain.club.sport.football.squad.api;

import com.official.lockr.domain.club.club.domain.event.AddedClubMemberEvent;
import com.official.lockr.domain.club.club.domain.event.FoundClubEvent;
import com.official.lockr.domain.club.club.domain.event.RemovedClubMemberEvent;
import com.official.lockr.domain.club.sport.football.squad.application.command.AddFootBallPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.application.command.CreateSquadCommand;
import com.official.lockr.domain.club.sport.football.squad.application.command.RemoveSquadPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.AddSquadPlayerUseCase;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.CreateSquadUseCase;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.RemoveSquadPlayerUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SquadEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SquadEventConsumer.class);

    private final AddSquadPlayerUseCase addSquadPlayerUseCase;
    private final CreateSquadUseCase createSquadUseCase;
    private final RemoveSquadPlayerUseCase removeSquadPlayerUseCase;
    private final RetryTemplate retryTemplate;

    public SquadEventConsumer(final AddSquadPlayerUseCase addSquadPlayerUseCase,
                              final CreateSquadUseCase createSquadUseCase,
                              final RemoveSquadPlayerUseCase removeSquadPlayerUseCase,
                              final RetryTemplate retryTemplate
    ) {
        this.addSquadPlayerUseCase = addSquadPlayerUseCase;
        this.createSquadUseCase = createSquadUseCase;
        this.removeSquadPlayerUseCase = removeSquadPlayerUseCase;
        this.retryTemplate = retryTemplate;
    }

    @TransactionalEventListener
    public void consume(final FoundClubEvent event) {
        if (!"FOOT_BALL".equals(event.sportType())) {
            return;
        }
        try {
            retryTemplate.execute(ctx -> {
                createSquadUseCase.create(new CreateSquadCommand(event.id(), event.foundUserId()));
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to create squad after all retries. clubId={}", event.id(), e);
        }
    }

    @TransactionalEventListener
    public void create(final AddedClubMemberEvent event) {
        if (!"FOOT_BALL".equals(event.sportType())) {
            return;
        }
        try {
            retryTemplate.execute(ctx -> {
                addSquadPlayerUseCase.addPlayer(new AddFootBallPlayerCommand(event.clubId(), event.userId()));
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to add squad player after all retries. clubId={}, userId={}",
                    event.clubId(), event.userId(), e);
        }
    }

    @TransactionalEventListener
    public void remove(final RemovedClubMemberEvent event) {
        try {
            retryTemplate.execute(ctx -> {
                removeSquadPlayerUseCase.removePlayer(new RemoveSquadPlayerCommand(event.clubId(), event.userId()));
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to remove squad player after all retries. clubId={}, userId={}",
                    event.clubId(), event.userId(), e);
        }
    }
}

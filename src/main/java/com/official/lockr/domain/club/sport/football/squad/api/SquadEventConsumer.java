package com.official.lockr.domain.club.sport.football.squad.api;

import com.official.lockr.domain.club.club.domain.event.AddedClubMemberEvent;
import com.official.lockr.domain.club.sport.football.squad.application.command.AddFootBallPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.application.usecase.AddSquadPlayerUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SquadEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SquadEventConsumer.class);

    private final AddSquadPlayerUseCase addSquadPlayerUseCase;
    private final RetryTemplate retryTemplate;

    public SquadEventConsumer(final AddSquadPlayerUseCase addSquadPlayerUseCase) {
        this.addSquadPlayerUseCase = addSquadPlayerUseCase;
        this.retryTemplate = RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(1000, 1.5, 5000)
                .retryOn(Exception.class)
                .build();
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
}

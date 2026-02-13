package com.official.lockr.domain.club.sport.football.lineup.api;

import com.official.lockr.domain.club.club.domain.event.FoundClubEvent;
import com.official.lockr.domain.club.sport.football.lineup.application.command.AddLineupsCommand;
import com.official.lockr.domain.club.sport.football.lineup.application.usecase.AddLineupUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LineupConsumer {

    private static final Logger log = LoggerFactory.getLogger(LineupConsumer.class);

    private final AddLineupUseCase addLineupUseCase;
    private final RetryTemplate retryTemplate;

    public LineupConsumer(final AddLineupUseCase addLineupUseCase) {
        this.addLineupUseCase = addLineupUseCase;
        this.retryTemplate = RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(1000, 1.5, 5000)
                .retryOn(Exception.class)
                .build();
    }

    @TransactionalEventListener
    public void consume(final FoundClubEvent event) {
        if (!"FOOT_BALL".equals(event.sportType())) {
            return;
        }
        try {
            retryTemplate.execute(ctx -> {
                addLineupUseCase.addAll(new AddLineupsCommand(event.id()));
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to add lineups after all retries. clubId={}", event.id(), e);
        }
    }
}

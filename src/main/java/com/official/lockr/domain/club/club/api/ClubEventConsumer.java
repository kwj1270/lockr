package com.official.lockr.domain.club.club.api;

import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import com.official.lockr.domain.club.club.application.usecase.RegisterClubMemberUseCase;
import com.official.lockr.domain.club.club.domain.MemberRole;
import com.official.lockr.domain.club.recruitment.applications.domain.event.ApprovedApplicationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ClubEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClubEventConsumer.class);

    private final RegisterClubMemberUseCase registerClubMemberUseCase;
    private final RetryTemplate retryTemplate;

    public ClubEventConsumer(final RegisterClubMemberUseCase registerClubMemberUseCase) {
        this.registerClubMemberUseCase = registerClubMemberUseCase;
        this.retryTemplate = RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(1000, 1.5, 5000)
                .retryOn(Exception.class)
                .build();
    }

    @TransactionalEventListener
    public void addMember(final ApprovedApplicationEvent event) {
        try {
            retryTemplate.execute(ctx -> {
                registerClubMemberUseCase.addMember(AddMemberCommand.basic(event.clubId(), event.userId(), null, event.profileImage()));
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to add club member after all retries. clubId={}, userId={}",
                    event.clubId(), event.userId(), e);
        }
    }
}

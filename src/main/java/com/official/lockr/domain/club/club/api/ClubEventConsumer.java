package com.official.lockr.domain.club.club.api;

import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import com.official.lockr.domain.club.club.application.usecase.RegisterClubMemberUseCase;
import com.official.lockr.domain.club.recruitment.applications.domain.event.ApprovedApplicationEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ClubEventConsumer {

    private final RegisterClubMemberUseCase registerClubMemberUseCase;

    public ClubEventConsumer(final RegisterClubMemberUseCase registerClubMemberUseCase) {
        this.registerClubMemberUseCase = registerClubMemberUseCase;
    }

    @TransactionalEventListener
    public void addMember(final ApprovedApplicationEvent event) {
        registerClubMemberUseCase.addMember(new AddMemberCommand(event.clubId(), event.userId(), event.profileImage()));
    }
}

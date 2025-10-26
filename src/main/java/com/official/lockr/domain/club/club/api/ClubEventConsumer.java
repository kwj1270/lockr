package com.official.lockr.domain.club.club.api;

import com.official.lockr.domain.club.contract.domain.event.ConcludedContractEvent;
import com.official.lockr.domain.club.club.application.RegisterClubMemberUseCase;
import com.official.lockr.domain.club.club.application.command.AddMemberCommand;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ClubEventConsumer {

    private final RegisterClubMemberUseCase registerClubMemberUseCase;

    public ClubEventConsumer(final RegisterClubMemberUseCase registerClubMemberUseCase) {
        this.registerClubMemberUseCase = registerClubMemberUseCase;
    }

    @EventListener
    public void addMember(final ConcludedContractEvent event) {
        registerClubMemberUseCase.addMember(new AddMemberCommand(event.clubId(), event.individualUserId()));
    }
}

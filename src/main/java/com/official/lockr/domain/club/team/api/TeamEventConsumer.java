package com.official.lockr.domain.club.team.api;

import com.official.lockr.domain.club.contract.domain.event.ConcludedContractEvent;
import com.official.lockr.domain.club.team.application.RegisterTeamMemberUseCase;
import com.official.lockr.domain.club.team.application.command.AddMemberCommand;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TeamEventConsumer {

    private final RegisterTeamMemberUseCase registerTeamMemberUseCase;

    public TeamEventConsumer(final RegisterTeamMemberUseCase registerTeamMemberUseCase) {
        this.registerTeamMemberUseCase = registerTeamMemberUseCase;
    }

    @EventListener
    public void addMember(final ConcludedContractEvent event) {
        registerTeamMemberUseCase.addMember(new AddMemberCommand(event.teamId(), event.individualUserId()));
    }
}

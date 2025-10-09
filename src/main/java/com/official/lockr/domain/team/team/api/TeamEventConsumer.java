package com.official.lockr.domain.team.team.api;

import com.official.lockr.domain.team.resume.domain.event.SignedContractEvent;
import com.official.lockr.domain.team.team.application.RegisterTeamMemberUseCase;
import com.official.lockr.domain.team.team.application.command.AddMemberCommand;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TeamEventConsumer {

    private final RegisterTeamMemberUseCase registerTeamMemberUseCase;

    public TeamEventConsumer(final RegisterTeamMemberUseCase registerTeamMemberUseCase) {
        this.registerTeamMemberUseCase = registerTeamMemberUseCase;
    }

    @EventListener
    public void register(final SignedContractEvent event) {
        registerTeamMemberUseCase.addMember(new AddMemberCommand(event.teamId(), event.individualUserId()));
    }
}

package com.official.lockr.domain.team.team.application;

import com.official.lockr.domain.team.team.application.command.AddMemberCommand;
import com.official.lockr.domain.team.team.domain.Team;

public interface RegisterTeamMemberUseCase {
    Team addMember(AddMemberCommand command);
}

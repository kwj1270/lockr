package com.official.lockr.domain.club.team.application;

import com.official.lockr.domain.club.team.application.command.AddMemberCommand;
import com.official.lockr.domain.club.team.domain.Team;

public interface RegisterTeamMemberUseCase {
    Team addMember(AddMemberCommand command);
}

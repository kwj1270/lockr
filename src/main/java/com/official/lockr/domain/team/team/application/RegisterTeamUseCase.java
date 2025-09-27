package com.official.lockr.domain.team.team.application;

import com.official.lockr.domain.team.team.application.command.RegisterTeamCommand;
import com.official.lockr.domain.team.team.domain.Team;

public interface RegisterTeamUseCase {
    Team register(final RegisterTeamCommand command);
}

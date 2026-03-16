package com.official.lockr.domain.team.team.application;

import com.official.lockr.domain.team.team.application.command.FoundTeamCommand;
import com.official.lockr.domain.team.team.domain.Team;

public interface FoundTeamUseCase {
    Team found(final FoundTeamCommand command);
}

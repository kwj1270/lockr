package com.official.lockr.domain.club.team.application;

import com.official.lockr.domain.club.team.application.command.FoundTeamCommand;
import com.official.lockr.domain.club.team.domain.Team;

public interface FoundTeamUseCase {
    Team found(final FoundTeamCommand command);
}

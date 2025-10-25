package com.official.lockr.domain.club.team.application;

import com.official.lockr.domain.club.team.application.command.AssignManagerCommand;
import com.official.lockr.domain.club.team.domain.Team;

public interface AssignMangerUseCase {
    Team assignManager(AssignManagerCommand command);
}

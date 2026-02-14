package com.official.lockr.domain.club.sport.football.squad.application.usecase;

import com.official.lockr.domain.club.sport.football.squad.application.command.CreateSquadCommand;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;

public interface CreateSquadUseCase {
    Squad create(final CreateSquadCommand command);
}

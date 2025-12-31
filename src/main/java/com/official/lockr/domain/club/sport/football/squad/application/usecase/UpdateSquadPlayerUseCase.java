package com.official.lockr.domain.club.sport.football.squad.application.usecase;

import com.official.lockr.domain.club.sport.football.squad.application.command.UpdateSquadPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;

public interface UpdateSquadPlayerUseCase {
    Squad updatePlayer(UpdateSquadPlayerCommand command);
}

package com.official.lockr.domain.club.sport.football.squad.application.usecase;

import com.official.lockr.domain.club.sport.football.squad.application.command.RemoveSquadPlayerCommand;

public interface RemoveSquadPlayerUseCase {
    void removePlayer(RemoveSquadPlayerCommand command);
}

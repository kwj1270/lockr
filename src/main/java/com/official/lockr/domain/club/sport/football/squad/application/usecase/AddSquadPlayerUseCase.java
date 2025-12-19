package com.official.lockr.domain.club.sport.football.squad.application.usecase;

import com.official.lockr.domain.club.sport.football.squad.application.dto.AddFootBallPlayerCommand;
import com.official.lockr.domain.club.sport.football.squad.domain.Squad;

public interface AddSquadPlayerUseCase {
    Squad addPlayer(final AddFootBallPlayerCommand command);
}

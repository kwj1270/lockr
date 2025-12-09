package com.official.lockr.domain.club.lineup.squad.application.usecase;

import com.official.lockr.domain.club.lineup.squad.application.dto.AddFootBallPlayerCommand;
import com.official.lockr.domain.club.lineup.squad.domain.Squad;

public interface AddFootBallPlayerUseCase {
    Squad addPlayer(final AddFootBallPlayerCommand command);
}

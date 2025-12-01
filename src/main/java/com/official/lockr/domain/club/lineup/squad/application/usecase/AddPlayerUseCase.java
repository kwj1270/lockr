package com.official.lockr.domain.club.lineup.squad.application.usecase;

import com.official.lockr.domain.club.lineup.squad.application.dto.AddPlayerCommand;
import com.official.lockr.domain.club.lineup.squad.domain.Squad;

public interface AddPlayerUseCase {
    Squad addPlayer(final AddPlayerCommand command);
}

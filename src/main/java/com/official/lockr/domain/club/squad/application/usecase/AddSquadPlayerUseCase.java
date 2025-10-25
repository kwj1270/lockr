package com.official.lockr.domain.club.squad.application.usecase;

import com.official.lockr.domain.club.squad.application.dto.AddSquadPlayerCommand;
import com.official.lockr.domain.club.squad.domain.squad.Squad;

public interface AddSquadPlayerUseCase {
    Squad addSquadPlayer(final AddSquadPlayerCommand command);
}

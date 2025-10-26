package com.official.lockr.domain.club.sqaud.application.usecase;

import com.official.lockr.domain.club.sqaud.application.dto.AddSquadPlayerCommand;
import com.official.lockr.domain.club.sqaud.domain.squad.Squad;

public interface AddSquadPlayerUseCase {
    Squad addSquadPlayer(final AddSquadPlayerCommand command);
}

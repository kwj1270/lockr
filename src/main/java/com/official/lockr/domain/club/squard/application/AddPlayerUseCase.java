package com.official.lockr.domain.club.squard.application;

import com.official.lockr.domain.club.squard.application.dto.AddPlayerCommand;
import com.official.lockr.domain.club.squard.domain.squad.Squad;

public interface AddPlayerUseCase {
    Squad addPlayer(final AddPlayerCommand command);
}

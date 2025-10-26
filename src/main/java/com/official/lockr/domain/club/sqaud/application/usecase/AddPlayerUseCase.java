package com.official.lockr.domain.club.sqaud.application.usecase;

import com.official.lockr.domain.club.sqaud.application.dto.AddPlayerCommand;
import com.official.lockr.domain.club.sqaud.domain.Squad;

public interface AddPlayerUseCase {
    Squad addPlayer(final AddPlayerCommand command);
}

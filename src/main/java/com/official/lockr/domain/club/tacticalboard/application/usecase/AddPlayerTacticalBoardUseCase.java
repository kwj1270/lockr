package com.official.lockr.domain.club.tacticalboard.application.usecase;

import com.official.lockr.domain.club.tacticalboard.application.dto.AddPlayerTacticalBoardCommand;
import com.official.lockr.domain.club.tacticalboard.domain.TacticalBoard;

public interface AddPlayerTacticalBoardUseCase {
    TacticalBoard addPlayer(final AddPlayerTacticalBoardCommand command);
}

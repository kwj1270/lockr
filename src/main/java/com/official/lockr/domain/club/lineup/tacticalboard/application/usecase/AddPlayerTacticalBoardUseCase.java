package com.official.lockr.domain.club.lineup.tacticalboard.application.usecase;

import com.official.lockr.domain.club.lineup.tacticalboard.application.dto.AddPlayerTacticalBoardCommand;
import com.official.lockr.domain.club.lineup.tacticalboard.domain.TacticalBoard;

public interface AddPlayerTacticalBoardUseCase {
    TacticalBoard addPlayer(final AddPlayerTacticalBoardCommand command);
}

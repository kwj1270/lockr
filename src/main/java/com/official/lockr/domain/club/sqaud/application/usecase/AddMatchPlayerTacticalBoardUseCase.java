package com.official.lockr.domain.club.sqaud.application.usecase;

import com.official.lockr.domain.club.sqaud.application.dto.AddMatchPlayerTacticalBoardCommand;
import com.official.lockr.domain.club.sqaud.domain.board.TacticalBoard;

public interface AddMatchPlayerTacticalBoardUseCase {
    TacticalBoard addMatchPlayer(final AddMatchPlayerTacticalBoardCommand command);
}

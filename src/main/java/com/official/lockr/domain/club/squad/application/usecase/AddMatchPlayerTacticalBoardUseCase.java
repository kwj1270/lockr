package com.official.lockr.domain.club.squad.application.usecase;

import com.official.lockr.domain.club.squad.application.dto.AddMatchPlayerTacticalBoardCommand;
import com.official.lockr.domain.club.squad.domain.board.TacticalBoard;

public interface AddMatchPlayerTacticalBoardUseCase {
    TacticalBoard addMatchPlayer(final AddMatchPlayerTacticalBoardCommand command);
}

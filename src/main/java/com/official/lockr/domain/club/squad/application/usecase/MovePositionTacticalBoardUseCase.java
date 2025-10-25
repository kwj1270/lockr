package com.official.lockr.domain.club.squad.application.usecase;

import com.official.lockr.domain.club.squad.application.dto.MoveLocationTacticalBoardCommand;
import com.official.lockr.domain.club.squad.domain.board.TacticalBoard;

public interface MovePositionTacticalBoardUseCase {
    TacticalBoard moveLocation(MoveLocationTacticalBoardCommand command);
}

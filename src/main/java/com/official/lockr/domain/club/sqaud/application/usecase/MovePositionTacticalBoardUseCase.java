package com.official.lockr.domain.club.sqaud.application.usecase;

import com.official.lockr.domain.club.sqaud.application.dto.MoveLocationTacticalBoardCommand;
import com.official.lockr.domain.club.sqaud.domain.board.TacticalBoard;

public interface MovePositionTacticalBoardUseCase {
    TacticalBoard moveLocation(MoveLocationTacticalBoardCommand command);
}

package com.official.lockr.domain.club.board.application.usecase;

import com.official.lockr.domain.club.board.application.dto.MoveLocationTacticalBoardCommand;
import com.official.lockr.domain.club.board.domain.TacticalBoard;

public interface MoveLocationTacticalBoardUseCase {
    TacticalBoard moveLocation(MoveLocationTacticalBoardCommand command);
}

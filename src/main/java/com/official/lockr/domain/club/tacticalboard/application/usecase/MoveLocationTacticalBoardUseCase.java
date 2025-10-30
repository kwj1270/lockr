package com.official.lockr.domain.club.tacticalboard.application.usecase;

import com.official.lockr.domain.club.tacticalboard.application.dto.MoveLocationTacticalBoardCommand;
import com.official.lockr.domain.club.tacticalboard.domain.TacticalBoard;

public interface MoveLocationTacticalBoardUseCase {
    TacticalBoard moveLocation(MoveLocationTacticalBoardCommand command);
}

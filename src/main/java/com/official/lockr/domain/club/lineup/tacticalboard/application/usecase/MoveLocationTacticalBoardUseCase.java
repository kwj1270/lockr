package com.official.lockr.domain.club.lineup.tacticalboard.application.usecase;

import com.official.lockr.domain.club.lineup.tacticalboard.application.dto.MoveLocationTacticalBoardCommand;
import com.official.lockr.domain.club.lineup.tacticalboard.domain.TacticalBoard;

public interface MoveLocationTacticalBoardUseCase {
    TacticalBoard moveLocation(MoveLocationTacticalBoardCommand command);
}

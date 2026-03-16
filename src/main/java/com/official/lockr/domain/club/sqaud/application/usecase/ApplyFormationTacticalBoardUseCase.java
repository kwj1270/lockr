package com.official.lockr.domain.club.sqaud.application.usecase;

import com.official.lockr.domain.club.sqaud.application.dto.ApplyFormationTacticalBoardCommand;
import com.official.lockr.domain.club.sqaud.domain.board.TacticalBoard;

public interface ApplyFormationTacticalBoardUseCase {
    TacticalBoard applyFormation(ApplyFormationTacticalBoardCommand command);
}

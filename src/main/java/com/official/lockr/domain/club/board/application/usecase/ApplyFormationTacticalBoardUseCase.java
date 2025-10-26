package com.official.lockr.domain.club.board.application.usecase;

import com.official.lockr.domain.club.board.application.dto.ApplyFormationTacticalBoardCommand;
import com.official.lockr.domain.club.board.domain.TacticalBoard;

public interface ApplyFormationTacticalBoardUseCase {
    TacticalBoard applyFormation(ApplyFormationTacticalBoardCommand command);
}

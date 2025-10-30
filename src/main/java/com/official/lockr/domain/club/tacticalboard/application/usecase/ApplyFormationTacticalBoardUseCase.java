package com.official.lockr.domain.club.tacticalboard.application.usecase;

import com.official.lockr.domain.club.tacticalboard.application.dto.ApplyFormationTacticalBoardCommand;
import com.official.lockr.domain.club.tacticalboard.domain.TacticalBoard;

public interface ApplyFormationTacticalBoardUseCase {
    TacticalBoard applyFormation(ApplyFormationTacticalBoardCommand command);
}

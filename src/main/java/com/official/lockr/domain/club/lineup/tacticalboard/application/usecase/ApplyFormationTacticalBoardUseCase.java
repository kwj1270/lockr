package com.official.lockr.domain.club.lineup.tacticalboard.application.usecase;

import com.official.lockr.domain.club.lineup.tacticalboard.application.dto.ApplyFormationTacticalBoardCommand;
import com.official.lockr.domain.club.lineup.tacticalboard.domain.TacticalBoard;

public interface ApplyFormationTacticalBoardUseCase {
    TacticalBoard applyFormation(ApplyFormationTacticalBoardCommand command);
}

package com.official.lockr.domain.club.tacticalboard.application.usecase;

import com.official.lockr.domain.club.tacticalboard.application.dto.CreateTacticalBoardCommand;
import com.official.lockr.domain.club.tacticalboard.domain.TacticalBoard;

public interface CreateTacticalBoardUseCase {
    TacticalBoard create(final CreateTacticalBoardCommand command);
}

package com.official.lockr.domain.club.lineup.tacticalboard.application.usecase;

import com.official.lockr.domain.club.lineup.tacticalboard.application.dto.CreateTacticalBoardCommand;
import com.official.lockr.domain.club.lineup.tacticalboard.domain.TacticalBoard;

public interface CreateTacticalBoardUseCase {
    TacticalBoard create(final CreateTacticalBoardCommand command);
}

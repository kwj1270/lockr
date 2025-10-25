package com.official.lockr.domain.club.squad.application.usecase;

import com.official.lockr.domain.club.squad.application.dto.CreateTacticalBoardCommand;
import com.official.lockr.domain.club.squad.domain.board.TacticalBoard;

public interface CreateTacticalBoardUseCase {
    TacticalBoard create(final CreateTacticalBoardCommand command);
}

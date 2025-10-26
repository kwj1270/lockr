package com.official.lockr.domain.club.sqaud.application.usecase;

import com.official.lockr.domain.club.sqaud.application.dto.CreateTacticalBoardCommand;
import com.official.lockr.domain.club.sqaud.domain.board.TacticalBoard;

public interface CreateTacticalBoardUseCase {
    TacticalBoard create(final CreateTacticalBoardCommand command);
}

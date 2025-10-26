package com.official.lockr.domain.club.board.application.usecase;

import com.official.lockr.domain.club.board.application.dto.CreateTacticalBoardCommand;
import com.official.lockr.domain.club.board.domain.TacticalBoard;

public interface CreateTacticalBoardUseCase {
    TacticalBoard create(final CreateTacticalBoardCommand command);
}

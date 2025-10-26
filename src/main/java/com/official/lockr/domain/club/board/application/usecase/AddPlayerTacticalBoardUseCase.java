package com.official.lockr.domain.club.board.application.usecase;

import com.official.lockr.domain.club.board.application.dto.AddPlayerTacticalBoardCommand;
import com.official.lockr.domain.club.board.domain.TacticalBoard;

public interface AddPlayerTacticalBoardUseCase {
    TacticalBoard addPlayer(final AddPlayerTacticalBoardCommand command);
}

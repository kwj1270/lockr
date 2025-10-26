package com.official.lockr.domain.club.board.application.usecase;

import com.official.lockr.domain.club.board.application.dto.SubstitutePlayerCommand;
import com.official.lockr.domain.club.board.domain.TacticalBoard;

public interface SubstitutePlayerUseCase {
    TacticalBoard substitute(final SubstitutePlayerCommand command);
}

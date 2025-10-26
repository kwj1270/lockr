package com.official.lockr.domain.club.sqaud.application.usecase;

import com.official.lockr.domain.club.sqaud.application.dto.SubstitutePlayerCommand;
import com.official.lockr.domain.club.sqaud.domain.board.TacticalBoard;

public interface SubstituteMatchPlayerUseCase {
    TacticalBoard substitute(final SubstitutePlayerCommand command);
}

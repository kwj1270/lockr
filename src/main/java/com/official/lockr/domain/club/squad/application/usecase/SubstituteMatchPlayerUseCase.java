package com.official.lockr.domain.club.squad.application.usecase;

import com.official.lockr.domain.club.squad.application.dto.SubstitutePlayerCommand;
import com.official.lockr.domain.club.squad.domain.board.TacticalBoard;

public interface SubstituteMatchPlayerUseCase {
    TacticalBoard substitute(final SubstitutePlayerCommand command);
}

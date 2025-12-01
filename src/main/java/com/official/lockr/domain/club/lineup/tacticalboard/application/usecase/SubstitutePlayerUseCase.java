package com.official.lockr.domain.club.lineup.tacticalboard.application.usecase;

import com.official.lockr.domain.club.lineup.tacticalboard.application.dto.SubstitutePlayerCommand;
import com.official.lockr.domain.club.lineup.tacticalboard.domain.TacticalBoard;

public interface SubstitutePlayerUseCase {
    TacticalBoard substitute(final SubstitutePlayerCommand command);
}

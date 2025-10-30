package com.official.lockr.domain.club.tacticalboard.application.usecase;

import com.official.lockr.domain.club.tacticalboard.application.dto.SubstitutePlayerCommand;
import com.official.lockr.domain.club.tacticalboard.domain.TacticalBoard;

public interface SubstitutePlayerUseCase {
    TacticalBoard substitute(final SubstitutePlayerCommand command);
}

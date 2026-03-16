package com.official.lockr.domain.shorts.application.usecase;

import com.official.lockr.domain.shorts.application.command.ToggleShortsHeartCommand;
import com.official.lockr.domain.shorts.domain.Shorts;

public interface ToggleShortsHeartUseCase {

    Shorts toggle(ToggleShortsHeartCommand command);
}

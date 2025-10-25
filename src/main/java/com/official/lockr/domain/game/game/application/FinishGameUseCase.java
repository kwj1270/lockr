package com.official.lockr.domain.game.game.application;

import com.official.lockr.domain.game.game.domain.Game;

public interface FinishGameUseCase {
    Game finish(String gameId, int totalMinutes);
}

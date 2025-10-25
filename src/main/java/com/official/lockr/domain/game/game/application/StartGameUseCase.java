package com.official.lockr.domain.game.game.application;

import com.official.lockr.domain.game.game.domain.Game;

public interface StartGameUseCase {
    Game start(final String gameId);
}

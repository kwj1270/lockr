package com.official.lockr.domain.game.game.domain;

public interface GameRepository {
    Game find(final String id);

    Game save(final Game game);
}

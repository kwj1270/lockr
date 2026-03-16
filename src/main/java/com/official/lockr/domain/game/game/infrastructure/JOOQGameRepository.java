package com.official.lockr.domain.game.game.infrastructure;

import com.official.lockr.domain.game.game.domain.Game;
import com.official.lockr.domain.game.game.domain.GameRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JOOQGameRepository implements GameRepository {

    @Override
    public Game find(final String id) {
        return null;
    }

    @Override
    public Game save(final Game game) {
        return null;
    }
}

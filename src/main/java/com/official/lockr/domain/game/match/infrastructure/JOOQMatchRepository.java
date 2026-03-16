package com.official.lockr.domain.game.match.infrastructure;

import com.official.lockr.domain.game.match.domain.Match;
import com.official.lockr.domain.game.match.domain.MatchRepository;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

@Component
public class JOOQMatchRepository implements MatchRepository {
    @Override
    public Match save(final Match match) {
        return null;
    }

    @Nullable
    @Override
    public Match find(final String id) {
        return null;
    }
}

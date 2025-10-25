package com.official.lockr.domain.game.match.domain;

import jakarta.annotation.Nullable;

public interface MatchRepository {
    Match save(final Match match);

    @Nullable
    Match find(final String id);
}

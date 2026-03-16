package com.official.lockr.domain.club.match.domain;

import jakarta.annotation.Nullable;

public interface MatchRepository {
    Match save(final Match match);

    @Nullable
    Match find(final String id);
}

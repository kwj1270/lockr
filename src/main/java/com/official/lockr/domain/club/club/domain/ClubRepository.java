package com.official.lockr.domain.club.club.domain;

import jakarta.annotation.Nullable;

public interface ClubRepository {
    @Nullable
    Club findByName(final String name);

    Club save(final Club club);

    @Nullable
    Club findById(String id);
}

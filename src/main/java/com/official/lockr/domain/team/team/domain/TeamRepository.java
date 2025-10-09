package com.official.lockr.domain.team.team.domain;

import jakarta.annotation.Nullable;

public interface TeamRepository {
    @Nullable Team findByName(final String name);

    Team save(final Team team);

    @Nullable Team findById(String id);
}

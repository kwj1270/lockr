package com.official.lockr.domain.team.team.domain;

public interface TeamRepository {
    Team findByName(final String name);

    Team save(final Team team);
}

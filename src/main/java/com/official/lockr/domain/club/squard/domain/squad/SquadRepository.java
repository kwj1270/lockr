package com.official.lockr.domain.club.squard.domain.squad;

public interface SquadRepository {
    Squad findByTeamId(final String teamId);

    Squad save(Squad squad);
}

package com.official.lockr.domain.club.squad.domain.squad;

public interface SquadRepository {
    Squad findByTeamId(final String teamId);

    Squad save(Squad squad);
}

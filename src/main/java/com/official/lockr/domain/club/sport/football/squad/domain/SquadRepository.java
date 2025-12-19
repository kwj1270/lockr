package com.official.lockr.domain.club.sport.football.squad.domain;

public interface SquadRepository {
    Squad findByClubId(final String clubId);

    Squad save(Squad lineUp);
}

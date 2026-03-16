package com.official.lockr.domain.club.sport.football.squad.domain;

public interface SquadRepository {
    Squad findById(final String squadId);

    Squad findByClubId(final String clubId);

    Squad save(final Squad squad);
}

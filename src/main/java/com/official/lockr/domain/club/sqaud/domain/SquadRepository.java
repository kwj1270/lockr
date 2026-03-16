package com.official.lockr.domain.club.sqaud.domain;

public interface SquadRepository {
    Squad findByClubId(final String clubId);

    Squad save(Squad squad);
}

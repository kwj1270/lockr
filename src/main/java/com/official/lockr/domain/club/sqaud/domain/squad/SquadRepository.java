package com.official.lockr.domain.club.sqaud.domain.squad;

public interface SquadRepository {
    Squad findByClubId(final String clubId);

    Squad save(Squad squad);
}

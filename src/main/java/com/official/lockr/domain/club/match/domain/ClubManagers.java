package com.official.lockr.domain.club.match.domain;

public interface ClubManagers {
    ClubManager find(final String clubId, final String clubManagerUserId);
}

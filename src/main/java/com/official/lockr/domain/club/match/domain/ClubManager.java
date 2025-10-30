package com.official.lockr.domain.club.match.domain;

public class ClubManager {
    private final String userId;
    private final String clubId;

    public ClubManager(final String userId, final String clubId) {
        this.userId = userId;
        this.clubId = clubId;
    }

    public String getUserId() {
        return userId;
    }

    public String getClubId() {
        return clubId;
    }
}

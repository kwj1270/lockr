package com.official.lockr.domain.club.lineup.tacticalboard.domain;

public class Coach {

    private final String userId;

    public Coach(final String userId) {
        this.userId = userId;
    }

    public String userId() {
        return userId;
    }
}

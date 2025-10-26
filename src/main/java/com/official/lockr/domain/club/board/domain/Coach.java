package com.official.lockr.domain.club.board.domain;

public class Coach {

    private final String userId;

    public Coach(final String userId) {
        this.userId = userId;
    }

    public String userId() {
        return userId;
    }
}

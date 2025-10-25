package com.official.lockr.domain.game.match.domain;

public class Manager {
    private final String userId;
    private final String teamId;
    private final RepresentativeRole role;

    public Manager(final String userId, final String teamId, final RepresentativeRole role) {
        this.userId = userId;
        this.teamId = teamId;
        this.role = role;
    }

    public boolean isNotAuthorized() {
        return false;
    }
}

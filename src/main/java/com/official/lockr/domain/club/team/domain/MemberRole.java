package com.official.lockr.domain.club.team.domain;

public enum MemberRole {
    PLAYER,
    COACH,
    MANAGER,
    TREASURER,
    VICE_PRESIDENT,
    PRESIDENT,
    ;

    public boolean isPresident() {
        return this == VICE_PRESIDENT || this == PRESIDENT;
    }

    public boolean isManager() {
        return this == MANAGER;
    }
}

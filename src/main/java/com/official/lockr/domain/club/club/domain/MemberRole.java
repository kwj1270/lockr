package com.official.lockr.domain.club.club.domain;

public enum MemberRole {
    BASIC,
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

    public boolean isStaff() {
        return this == COACH || this == MANAGER || this == TREASURER || this == VICE_PRESIDENT || this == PRESIDENT;
    }

    public boolean isBasic() {
        return !isStaff();
    }
}

package com.official.lockr.domain.team.resume.domain;

public class Representative {

    private final String userId;
    private final String memberRole;

    public Representative(final String userId, final String memberRole) {
        this.userId = userId;
        this.memberRole = memberRole;
    }

    public String getUserId() {
        return userId;
    }

    public String getMemberRole() {
        return memberRole;
    }
}

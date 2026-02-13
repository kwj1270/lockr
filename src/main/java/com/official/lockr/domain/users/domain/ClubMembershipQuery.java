package com.official.lockr.domain.users.domain;

public interface ClubMembershipQuery {
    boolean hasActiveClubMembership(String userId);
}

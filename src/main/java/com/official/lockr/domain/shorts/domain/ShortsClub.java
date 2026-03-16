package com.official.lockr.domain.shorts.domain;

import jakarta.annotation.Nullable;

public interface ShortsClub {

    @Nullable
    ShortsMember findMemberByUserIdAndClubId(String userId, String clubId);
}

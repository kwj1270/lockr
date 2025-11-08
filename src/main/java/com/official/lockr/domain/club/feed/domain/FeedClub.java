package com.official.lockr.domain.club.feed.domain;

import com.official.lockr.domain.club.club.domain.Member;
import jakarta.annotation.Nullable;

public interface FeedClub {

    @Nullable
    Member findMemberByUserIdAndClubId(String userId, String clubId);
}

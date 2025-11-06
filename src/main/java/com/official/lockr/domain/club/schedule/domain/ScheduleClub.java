package com.official.lockr.domain.club.schedule.domain;

import com.official.lockr.domain.club.club.domain.Member;
import jakarta.annotation.Nullable;

import java.util.List;

public interface ScheduleClub {

    @Nullable
    Member findMemberByUserIdAndClubId(String userId, String clubId);

    List<Member> findAllMemberIdsByClubId(String clubId);
}

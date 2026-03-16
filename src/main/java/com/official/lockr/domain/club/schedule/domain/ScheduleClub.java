package com.official.lockr.domain.club.schedule.domain;

import com.official.lockr.domain.club.club.domain.Member;
import jakarta.annotation.Nullable;

import java.util.List;

public interface ScheduleClub {

    @Nullable
    Member findMemberByUserIdAndClubId(String userId, String clubId);

    List<Member> findAllMemberIdsByClubId(String clubId);

    boolean isStaff(String userId, String clubId);

    boolean isMember(String userId, String clubId);

    @Nullable
    String findStaffRoleName(String userId, String clubId);

    @Nullable
    String findClubNameById(String clubId);

    boolean existsClub(String clubId);

    List<String> findAllUserIdsByClubId(String clubId);
}

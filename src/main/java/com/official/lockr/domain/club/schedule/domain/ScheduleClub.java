package com.official.lockr.domain.club.schedule.domain;

import jakarta.annotation.Nullable;

import java.util.List;

public interface ScheduleClub {

    boolean isStaff(String userId, String clubId);

    boolean isMember(String userId, String clubId);

    @Nullable
    String findStaffRoleName(String userId, String clubId);

    @Nullable
    String findClubNameById(String clubId);

    boolean existsClub(String clubId);

    List<String> findAllUserIdsByClubId(String clubId);
}

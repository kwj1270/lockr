package com.official.lockr.domain.club.schedule.domain;

import jakarta.annotation.Nullable;

import java.time.YearMonth;
import java.util.List;

public interface ScheduleRepository {

    @Nullable
    Schedule findById(String id);

    List<Schedule> findAllByClubId(String clubId);

    List<Schedule> findAllByClubIdAndMonth(String clubId, YearMonth yearMonth);

    Schedule save(Schedule schedule);

    void delete(String id);
}

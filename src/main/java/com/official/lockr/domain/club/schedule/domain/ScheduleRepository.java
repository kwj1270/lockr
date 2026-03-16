package com.official.lockr.domain.club.schedule.domain;

import jakarta.annotation.Nullable;

import java.time.YearMonth;
import java.util.List;

public interface ScheduleRepository {

    @Nullable
    Schedule findById(String id);

    Schedule save(Schedule schedule);
}

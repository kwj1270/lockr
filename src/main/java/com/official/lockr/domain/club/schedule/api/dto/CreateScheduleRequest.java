package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.detail.ScheduleDetail;

import java.time.LocalDateTime;
import java.util.List;

public record CreateScheduleRequest(
        String title,
        String content,
        String location,
        LocalDateTime scheduleTime,
        ScheduleType scheduleType,
        String detail,
        List<String> memberIds
) {
}

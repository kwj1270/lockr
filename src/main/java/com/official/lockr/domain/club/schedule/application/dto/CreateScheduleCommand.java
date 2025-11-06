package com.official.lockr.domain.club.schedule.application.dto;

import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.detail.ScheduleDetail;

import java.time.LocalDateTime;

public record CreateScheduleCommand(
        String userId,
        String clubId,
        String title,
        String content,
        String location,
        LocalDateTime scheduleTime,
        ScheduleType scheduleType,
        ScheduleDetail detail
) {
}

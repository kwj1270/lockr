package com.official.lockr.domain.club.schedule.application.dto;

import com.official.lockr.domain.club.schedule.domain.detail.ScheduleDetail;

import java.time.LocalDateTime;

public record UpdateScheduleCommand(
        String scheduleId,
        String userId,
        String clubId,
        String title,
        String content,
        String location,
        LocalDateTime scheduleTime,
        ScheduleDetail detail
) {
}

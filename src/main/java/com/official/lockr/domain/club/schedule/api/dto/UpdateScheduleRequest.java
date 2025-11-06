package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.detail.ScheduleDetail;

import java.time.LocalDateTime;

public record UpdateScheduleRequest(
        String title,
        String content,
        String location,
        LocalDateTime scheduleTime,
        ScheduleDetail detail
) {
}

package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;

import java.time.LocalDateTime;

public record UpdateScheduleRequest(
        String title,
        String content,
        String location,
        LocalDateTime scheduleTime,
        ScheduleDetailData detail,
        int minParticipants,
        int maxParticipants,
        int deadlineDays
) {
}

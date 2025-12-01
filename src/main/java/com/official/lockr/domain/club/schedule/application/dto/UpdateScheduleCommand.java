package com.official.lockr.domain.club.schedule.application.dto;

import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;

import java.time.LocalDateTime;

public record UpdateScheduleCommand(
        String scheduleId,
        String userId,
        String clubId,
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

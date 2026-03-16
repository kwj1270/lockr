package com.official.lockr.domain.club.schedule.application.command;

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
        Integer minParticipants,
        int deadlineDays
) {
    public UpdateScheduleCommand {
        if (scheduleId == null || scheduleId.isBlank()) {
            throw new IllegalArgumentException("scheduleId must not be null or blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        if (clubId == null || clubId.isBlank()) {
            throw new IllegalArgumentException("clubId must not be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be null or blank");
        }
    }
}

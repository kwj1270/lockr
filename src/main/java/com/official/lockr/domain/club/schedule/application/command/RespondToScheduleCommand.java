package com.official.lockr.domain.club.schedule.application.command;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;

public record RespondToScheduleCommand(
        String scheduleId,
        String userId,
        String clubId,
        AttendanceStatus status,
        String reason
) {
    public RespondToScheduleCommand {
        if (scheduleId == null || scheduleId.isBlank()) {
            throw new IllegalArgumentException("scheduleId must not be null or blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        if (clubId == null || clubId.isBlank()) {
            throw new IllegalArgumentException("clubId must not be null or blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }
}

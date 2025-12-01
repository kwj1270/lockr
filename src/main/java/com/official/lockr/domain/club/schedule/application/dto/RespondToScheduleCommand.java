package com.official.lockr.domain.club.schedule.application.dto;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;

public record RespondToScheduleCommand(
        String scheduleId,
        String userId,
        String clubId,
        AttendanceStatus status,
        String reason
) {
}

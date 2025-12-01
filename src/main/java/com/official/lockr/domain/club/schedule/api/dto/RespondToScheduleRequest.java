package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;

public record RespondToScheduleRequest(
        AttendanceStatus status,
        String reason
) {
}

package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.application.command.RespondToScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record RespondToScheduleRequest(
        @NotNull AttendanceStatus status,
        String reason
) {
    public RespondToScheduleCommand toCommand(final String scheduleId, final String userId, final String clubId) {
        return new RespondToScheduleCommand(scheduleId, userId, clubId, status, reason);
    }
}

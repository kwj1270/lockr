package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.application.command.AdminUpdateAttendanceCommand;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AdminUpdateAttendanceRequest(
        @NotNull AttendanceStatus status,
        String reason
) {
    public AdminUpdateAttendanceCommand toCommand(
            final String scheduleId,
            final String adminUserId,
            final String clubId,
            final String targetUserId
    ) {
        return new AdminUpdateAttendanceCommand(scheduleId, adminUserId, clubId, targetUserId, status, reason);
    }
}

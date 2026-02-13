package com.official.lockr.domain.club.schedule.application.command;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;

public record AdminUpdateAttendanceCommand(
        String scheduleId,
        String adminUserId,
        String clubId,
        String targetUserId,
        AttendanceStatus status,
        String reason
) {
    public AdminUpdateAttendanceCommand {
        if (scheduleId == null || scheduleId.isBlank()) {
            throw new IllegalArgumentException("scheduleId must not be null or blank");
        }
        if (adminUserId == null || adminUserId.isBlank()) {
            throw new IllegalArgumentException("adminUserId must not be null or blank");
        }
        if (clubId == null || clubId.isBlank()) {
            throw new IllegalArgumentException("clubId must not be null or blank");
        }
        if (targetUserId == null || targetUserId.isBlank()) {
            throw new IllegalArgumentException("targetUserId must not be null or blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
    }
}

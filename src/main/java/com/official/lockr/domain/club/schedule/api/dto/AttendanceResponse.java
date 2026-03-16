package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.Attendance;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;

public record AttendanceResponse(
        String id,
        String userId,
        AttendanceStatus status,
        String reason
) {
    public static AttendanceResponse from(final Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getUserId(),
                attendance.getStatus(),
                attendance.getReason()
        );
    }
}

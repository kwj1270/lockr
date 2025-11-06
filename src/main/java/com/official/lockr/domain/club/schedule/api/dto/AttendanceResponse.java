package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.attendance.Attendance;
import com.official.lockr.domain.club.schedule.domain.attendance.AttendanceStatus;

import java.time.LocalDateTime;

public record AttendanceResponse(
        String id,
        String userId,
        AttendanceStatus status,
        String reason,
        LocalDateTime respondedAt
) {
    public static AttendanceResponse from(final Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getUserId(),
                attendance.getStatus(),
                attendance.getReason(),
                attendance.getRespondedAt()
        );
    }
}

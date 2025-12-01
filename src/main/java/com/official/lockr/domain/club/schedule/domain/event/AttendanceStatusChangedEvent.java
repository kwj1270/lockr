package com.official.lockr.domain.club.schedule.domain.event;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record AttendanceStatusChangedEvent(
        String attendanceId,
        String scheduleId,
        String userId,
        String changedBy,
        String changedByRole,
        AttendanceStatus previousStatus,
        AttendanceStatus newStatus,
        String reason,
        LocalDateTime changedAt
) implements DomainEvent {

    public static AttendanceStatusChangedEvent of(
            final String attendanceId,
            final String scheduleId,
            final String userId,
            final AttendanceStatus previousStatus,
            final AttendanceStatus newStatus
    ) {
        return new AttendanceStatusChangedEvent(
                attendanceId,
                scheduleId,
                userId,
                userId, // 본인이 변경한 경우
                "PLAYER", // 기본값
                previousStatus,
                newStatus,
                null,
                LocalDateTime.now()
        );
    }
}

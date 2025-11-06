package com.official.lockr.domain.club.schedule.domain.event;

import com.official.lockr.domain.club.schedule.domain.attendance.AttendanceStatus;
import com.official.lockr.global.ddd.DomainEvent;

public record RespondedToScheduleEvent(
        String scheduleId,
        String clubId,
        String userId,
        AttendanceStatus previousStatus,
        AttendanceStatus newStatus
) implements DomainEvent {
}
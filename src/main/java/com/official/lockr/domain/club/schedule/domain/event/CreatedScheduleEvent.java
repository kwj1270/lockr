package com.official.lockr.domain.club.schedule.domain.event;

import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record CreatedScheduleEvent(
        String scheduleId,
        String clubId,
        ScheduleType scheduleType,
        LocalDateTime scheduleTime
) implements DomainEvent {
}
package com.official.lockr.domain.club.schedule.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record UpdatedScheduleEvent(
        String scheduleId,
        String clubId,
        LocalDateTime newScheduleTime,
        String title,
        String location
) implements DomainEvent {
}
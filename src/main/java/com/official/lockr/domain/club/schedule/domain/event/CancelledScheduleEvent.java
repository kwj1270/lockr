package com.official.lockr.domain.club.schedule.domain.event;

import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.global.ddd.DomainEvent;

public record CancelledScheduleEvent(
        String scheduleId,
        String clubId,
        ScheduleType scheduleType
) implements DomainEvent {
}

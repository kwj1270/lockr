package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;

import java.time.LocalDateTime;

public record ScheduleItemResponse(
    String id,
    String clubId,
    String title,
    String content,
    ScheduleLocationResponse location,
    LocalDateTime scheduleTime,
    ScheduleType type,
    ScheduleStatus status,
    int attendingCount,
    int notAttendingCount,
    int noResponseCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

package com.official.lockr.domain.club.schedule.api.dto;

import java.time.LocalDateTime;

public record ScheduleItemResponse(
    String id,
    String clubId,
    String title,
    String content,
    String location,
    LocalDateTime scheduleTime,
    String type,
    String status,
    int attendingCount,
    int notAttendingCount,
    int noResponseCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

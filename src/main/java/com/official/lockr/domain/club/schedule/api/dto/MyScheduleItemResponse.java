package com.official.lockr.domain.club.schedule.api.dto;

import java.time.LocalDateTime;

public record MyScheduleItemResponse(
    String id,
    String clubId,
    String clubName,
    String sport,
    String title,
    String type,
    LocalDateTime scheduleTime,
    ScheduleLocationResponse location,
    int attendingCount,
    int notAttendingCount,
    int noResponseCount,
    Integer maxParticipants,
    String myAttendanceStatus,
    String status
) {
}

package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleStatus;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleDetailResponse(
    String id,
    String clubId,
    String title,
    String content,
    ScheduleLocationResponse location,
    LocalDateTime scheduleTime,
    ScheduleType type,
    String detailData,
    ScheduleStatus status,
    Integer minParticipants,
    Integer maxParticipants,
    Integer deadlineDays,
    List<AttendanceItemResponse> attendances,
    int attendingCount,
    int notAttendingCount,
    int noResponseCount,
    AttendanceStatus myAttendanceStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

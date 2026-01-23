package com.official.lockr.domain.club.schedule.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleDetailResponse(
    String id,
    String clubId,
    String title,
    String content,
    ScheduleLocationResponse location,
    LocalDateTime scheduleTime,
    String type,
    String detailData,
    String status,
    Integer minParticipants,
    Integer maxParticipants,
    Integer deadlineDays,
    List<AttendanceItemResponse> attendances,
    int attendingCount,
    int notAttendingCount,
    int noResponseCount,
    String myAttendanceStatus,  // 현재 사용자의 참석 상태
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

package com.official.lockr.domain.club.schedule.api.dto;

public record AttendanceItemResponse(
    String id,
    String userId,
    String userName,
    String status,
    String reason
) {
}

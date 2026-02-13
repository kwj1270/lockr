package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;

public record AttendanceItemResponse(
    String id,
    String userId,
    String userName,
    AttendanceStatus status,
    String reason
) {
}

package com.official.lockr.domain.club.schedule.api.dto;

import java.time.LocalDateTime;

public record ScheduleCommentItemResponse(
        String id,
        String scheduleId,
        String userId,
        String userName,
        String profileImage,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

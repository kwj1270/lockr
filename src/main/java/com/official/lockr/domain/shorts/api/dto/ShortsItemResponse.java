package com.official.lockr.domain.shorts.api.dto;

import java.time.LocalDateTime;

public record ShortsItemResponse(
        String id,
        String clubId,
        String clubName,
        String userId,
        String userName,
        String userProfileImage,
        String title,
        String videoUrl,
        String thumbnailUrl,
        int duration,
        long viewCount,
        int heartsCount,
        int commentsCount,
        boolean isHearted,
        LocalDateTime createdAt
) {
}

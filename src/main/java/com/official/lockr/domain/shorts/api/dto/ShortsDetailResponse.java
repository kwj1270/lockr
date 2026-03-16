package com.official.lockr.domain.shorts.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ShortsDetailResponse(
        String id,
        String clubId,
        String clubName,
        String userId,
        String userName,
        String userProfileImage,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        int duration,
        long viewCount,
        int heartsCount,
        int commentsCount,
        boolean isHearted,
        List<ShortsCommentResponse> comments,
        LocalDateTime createdAt
) {
}

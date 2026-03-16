package com.official.lockr.domain.shorts.api.dto;

import java.time.LocalDateTime;

public record ShortsCommentResponse(
        String id,
        String userId,
        String userName,
        String userProfileImage,
        String content,
        LocalDateTime createdAt
) {
}

package com.official.lockr.domain.club.feed.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record FeedItemResponse(
    String id,
    String clubId,
    String clubName,
    String userId,
    String userName,
    String authorRole,
    String title,
    String content,
    String feedType,
    List<String> imageUrls,
    List<String> videoUrls,
    int heartsCount,
    int commentsCount,
    boolean isLikedByMe,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

package com.official.lockr.domain.club.feed.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentItemResponse(
    String id,
    String feedId,
    String userId,
    String userName,
    String content,
    String parentCommentId,
    List<String> imageUrls,
    List<String> videoUrls,
    int heartsCount,
    boolean isLikedByMe,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

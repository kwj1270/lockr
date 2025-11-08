package com.official.lockr.domain.club.feed.api.dto;

import com.official.lockr.domain.club.feed.domain.comment.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        String id,
        String feedId,
        String userId,
        String content,
        List<String> imageUrls,
        List<String> videoUrls,
        int heartsCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponse from(final Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getFeedId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getImages().getUrls(),
                comment.getVideos().getUrls(),
                comment.getHearts().size(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}

package com.official.lockr.domain.club.feed.api.dto;

import com.official.lockr.domain.club.feed.domain.Feed;
import com.official.lockr.domain.club.feed.domain.FeedType;

import java.time.LocalDateTime;
import java.util.List;

public record FeedResponse(
        String id,
        String clubId,
        String userId,
        String content,
        FeedType feedType,
        List<String> imageUrls,
        List<String> videoUrls,
        List<CommentResponse> comments,
        int heartsCount,
        int commentsCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FeedResponse from(final Feed feed) {
        return new FeedResponse(
                feed.getId(),
                feed.getClubId(),
                feed.getUserId(),
                feed.getContent(),
                feed.getFeedType(),
                feed.getImages().getUrls(),
                feed.getVideos().getUrls(),
                feed.getActiveComments().stream()
                        .map(CommentResponse::from)
                        .toList(),
                feed.getHeartsCount(),
                feed.getCommentsCount(),
                feed.getCreatedAt(),
                feed.getUpdatedAt()
        );
    }
}

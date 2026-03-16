package com.official.lockr.domain.club.feed.api.dto;

import com.official.lockr.domain.club.feed.domain.entity.Heart;

import java.time.LocalDateTime;

public record HeartResponse(
        String id,
        String feedId,
        String userId,
        LocalDateTime createdAt
) {
    public static HeartResponse from(final Heart heart) {
        return new HeartResponse(
                heart.getId(),
                heart.getFeedId(),
                heart.getUserId(),
                heart.getCreatedAt()
        );
    }
}

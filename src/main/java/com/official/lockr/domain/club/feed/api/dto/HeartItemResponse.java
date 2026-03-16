package com.official.lockr.domain.club.feed.api.dto;

import java.time.LocalDateTime;

public record HeartItemResponse(
    String id,
    String feedId,
    String userId,
    String userName,
    LocalDateTime createdAt
) {
}

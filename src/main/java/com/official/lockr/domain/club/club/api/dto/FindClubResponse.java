package com.official.lockr.domain.club.club.api.dto;

import java.time.LocalDateTime;

public record FindClubResponse(
        String id,
        String foundUserId,
        String name,
        String sportType,
        String city,
        String district,
        String description,
        String profileImageUrl,
        String backgroundImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
}

package com.official.lockr.domain.home.banner.api.dto;

import java.time.LocalDateTime;

public record AdminBannerResponse(
        String id,
        String title,
        String subtitle,
        String imageUrl,
        String actionUrl,
        String actionRoute,
        String bgColor,
        String textColor,
        String iconType,
        int displayOrder,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

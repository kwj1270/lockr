package com.official.lockr.domain.home.banner.api.dto;

import com.official.lockr.domain.home.banner.domain.Banner;

public record BannerResponse(
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
        boolean isActive
) {
    public static BannerResponse from(final Banner banner) {
        return new BannerResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getSubtitle(),
                banner.getImageUrl(),
                banner.getActionUrl(),
                banner.getActionRoute(),
                banner.getBgColor(),
                banner.getTextColor(),
                banner.getIconType(),
                banner.getDisplayOrder(),
                banner.isActive()
        );
    }
}

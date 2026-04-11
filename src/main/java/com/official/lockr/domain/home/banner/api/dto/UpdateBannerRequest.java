package com.official.lockr.domain.home.banner.api.dto;

import com.official.lockr.domain.home.banner.application.command.UpdateBannerCommand;

public record UpdateBannerRequest(
        String title,
        String subtitle,
        String imageUrl,
        String actionUrl,
        String actionRoute,
        String bgColor,
        String textColor,
        String iconType,
        int displayOrder
) {
    public UpdateBannerCommand toCommand(final String bannerId) {
        return new UpdateBannerCommand(
                bannerId, title, subtitle,
                imageUrl, actionUrl, actionRoute,
                bgColor, textColor, iconType, displayOrder
        );
    }
}

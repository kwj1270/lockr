package com.official.lockr.domain.home.banner.api.dto;

import com.official.lockr.domain.home.banner.application.command.CreateBannerCommand;

public record CreateBannerRequest(
        String title,
        String subtitle,
        String placement,
        String imageUrl,
        String actionUrl,
        String actionRoute,
        String bgColor,
        String textColor,
        String iconType,
        int displayOrder
) {
    public CreateBannerCommand toCommand() {
        return new CreateBannerCommand(
                title, subtitle, placement,
                imageUrl, actionUrl, actionRoute,
                bgColor, textColor, iconType, displayOrder
        );
    }
}

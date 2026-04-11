package com.official.lockr.domain.home.banner.application.command;

public record UpdateBannerCommand(
        String bannerId,
        String title,
        String subtitle,
        String imageUrl,
        String actionUrl,
        String actionRoute,
        String bgColor,
        String textColor,
        String iconType,
        int displayOrder
) {}

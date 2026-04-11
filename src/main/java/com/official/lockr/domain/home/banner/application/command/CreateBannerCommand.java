package com.official.lockr.domain.home.banner.application.command;

public record CreateBannerCommand(
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
) {}

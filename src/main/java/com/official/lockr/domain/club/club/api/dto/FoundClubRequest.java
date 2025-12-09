package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.FoundClubCommand;

public record FoundClubRequest(
        String name,
        String sportType,
        String city,
        String district,
        String description,
        String profileImageUrl,
        String backgroundImageUrl
) {
    public FoundClubCommand toCommand(final String userId) {
        return new FoundClubCommand(userId, name, sportType, city, district, description, profileImageUrl, backgroundImageUrl);
    }
}

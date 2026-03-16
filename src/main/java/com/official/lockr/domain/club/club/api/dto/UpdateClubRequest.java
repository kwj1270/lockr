package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.UpdateClubCommand;

public record UpdateClubRequest(
        String name,
        String description,
        String city,
        String district,
        String profileImageUrl,
        String backgroundImageUrl
) {
    public UpdateClubCommand toCommand(final String clubId, final String userId) {
        return new UpdateClubCommand(clubId, userId, name, description, city, district, profileImageUrl, backgroundImageUrl);
    }
}

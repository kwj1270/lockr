package com.official.lockr.domain.club.club.application.command;

public record UpdateClubCommand(
        String clubId,
        String userId,
        String name,
        String description,
        String city,
        String district,
        String profileImageUrl,
        String backgroundImageUrl
) {
}

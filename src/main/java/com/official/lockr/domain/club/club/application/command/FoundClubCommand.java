package com.official.lockr.domain.club.club.application.command;

public record FoundClubCommand(
        String userId,
        String name,
        String sportType,
        String city,
        String district,
        String description,
        String profileImageUrl,
        String backgroundImageUrl
) {

}

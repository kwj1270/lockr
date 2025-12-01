package com.official.lockr.domain.club.club.application.command;

public record FoundClubCommand(
        String userId,
        String name,
        String description,
        String region,
        String sportType,
        String profileImageUrl,
        String backgroundImageUrl
) {

}

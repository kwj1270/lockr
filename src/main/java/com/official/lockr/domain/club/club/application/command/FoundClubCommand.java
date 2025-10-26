package com.official.lockr.domain.club.club.application.command;

public record FoundClubCommand(
        String userId,
        String name,
        String description
) {

}

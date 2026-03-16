package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.FoundClubCommand;

public record FoundClubRequest(
        String name,
        String description
) {
    public FoundClubCommand toCommand(final String userId) {
        return new FoundClubCommand(userId, name, description);
    }
}

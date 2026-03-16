package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.ChangeVisibilityCommand;

public record ChangeVisibilityRequest(
        boolean isPublic
) {

    public ChangeVisibilityCommand toCommand(final String clubId, final String userId) {
        return new ChangeVisibilityCommand(clubId, userId, isPublic);
    }
}

package com.official.lockr.domain.club.club.application.command;

public record ChangeVisibilityCommand(
        String clubId,
        String userId,
        boolean isPublic
) {
}

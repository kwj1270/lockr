package com.official.lockr.domain.club.club.application.command;

public record AssignCoachCommand(
        String clubId,
        String userId,
        String targetUserId
) {
}

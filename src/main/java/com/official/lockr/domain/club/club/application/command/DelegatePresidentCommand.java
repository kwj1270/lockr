package com.official.lockr.domain.club.club.application.command;

public record DelegatePresidentCommand(
        String clubId,
        String userId,
        String targetUserId
) {
}

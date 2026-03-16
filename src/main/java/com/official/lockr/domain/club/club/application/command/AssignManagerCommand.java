package com.official.lockr.domain.club.club.application.command;

public record AssignManagerCommand(
        String clubId,
        String userId,
        String targetMemberId
) {
}

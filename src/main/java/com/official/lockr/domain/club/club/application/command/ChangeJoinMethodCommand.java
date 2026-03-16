package com.official.lockr.domain.club.club.application.command;

public record ChangeJoinMethodCommand(
        String clubId,
        String userId,
        String joinMethod
) {
}

package com.official.lockr.domain.club.team.application.command;

public record AssignManagerCommand(
        String teamId,
        String userId,
        String targetMemberId
) {
}

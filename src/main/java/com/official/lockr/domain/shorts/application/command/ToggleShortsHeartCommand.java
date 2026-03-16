package com.official.lockr.domain.shorts.application.command;

public record ToggleShortsHeartCommand(
        String shortsId,
        String userId
) {
}

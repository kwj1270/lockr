package com.official.lockr.domain.shorts.application.command;

public record RestoreShortsCommand(
        String shortsId,
        String userId,
        String clubId
) {
}

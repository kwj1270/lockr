package com.official.lockr.domain.shorts.application.command;

public record DeleteShortsCommand(
        String shortsId,
        String userId,
        String clubId
) {
}

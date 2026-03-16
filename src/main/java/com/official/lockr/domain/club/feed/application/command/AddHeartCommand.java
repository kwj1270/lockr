package com.official.lockr.domain.club.feed.application.command;

public record AddHeartCommand(
        String feedId,
        String userId,
        String clubId) {
}

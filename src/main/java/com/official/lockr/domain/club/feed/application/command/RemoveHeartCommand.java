package com.official.lockr.domain.club.feed.application.command;

public record RemoveHeartCommand(
        String feedId,
        String userId,
        String clubId) {
}

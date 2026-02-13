package com.official.lockr.domain.club.feed.application.command;

public record DeleteFeedCommand(
        String feedId,
        String userId,
        String clubId
) {
}

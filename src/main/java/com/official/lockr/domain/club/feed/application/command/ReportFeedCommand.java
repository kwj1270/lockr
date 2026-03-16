package com.official.lockr.domain.club.feed.application.command;

public record ReportFeedCommand(
        String feedId,
        String userId,
        String clubId,
        String reason
) {
}
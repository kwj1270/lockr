package com.official.lockr.domain.club.feed.api.dto;

import com.official.lockr.domain.club.feed.application.command.ReportFeedCommand;

public record ReportFeedRequest(
        String reason
) {
    public ReportFeedCommand toCommand(final String feedId, final String userId, final String clubId) {
        return new ReportFeedCommand(feedId, userId, clubId, reason);
    }
}

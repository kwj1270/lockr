package com.official.lockr.domain.club.feed.api.dto;

import com.official.lockr.domain.club.feed.application.dto.UpdateFeedCommand;

import java.util.List;

public record UpdateFeedRequest(
        String title,
        String content,
        List<String> imageUrls,
        List<String> videoUrls
) {
    public UpdateFeedCommand toCommand(final String feedId, final String userId, final String clubId) {
        return new UpdateFeedCommand(
                feedId, userId, clubId,
                title, content, imageUrls, videoUrls
        );
    }
}

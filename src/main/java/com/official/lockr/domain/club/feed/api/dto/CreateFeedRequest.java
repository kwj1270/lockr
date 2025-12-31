package com.official.lockr.domain.club.feed.api.dto;

import com.official.lockr.domain.club.feed.application.dto.CreateFeedCommand;
import com.official.lockr.domain.club.feed.domain.FeedType;

import java.util.List;

public record CreateFeedRequest(
        String title,
        String content,
        FeedType feedType,
        List<String> imageUrls,
        List<String> videoUrls
) {
    public CreateFeedCommand toCommand(final String userId, final String clubId) {
        return new CreateFeedCommand(
                userId, clubId,
                title, content, feedType,
                imageUrls, videoUrls
        );
    }
}

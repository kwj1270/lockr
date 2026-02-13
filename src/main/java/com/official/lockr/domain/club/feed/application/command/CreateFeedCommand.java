package com.official.lockr.domain.club.feed.application.command;

import com.official.lockr.domain.club.feed.domain.FeedType;

import java.util.List;

public record CreateFeedCommand(
        String userId,
        String clubId,
        String title,
        String content,
        FeedType feedType,
        List<String> imageUrls,
        List<String> videoUrls
) {
}

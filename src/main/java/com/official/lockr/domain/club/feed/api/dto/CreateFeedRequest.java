package com.official.lockr.domain.club.feed.api.dto;

import com.official.lockr.domain.club.feed.domain.FeedType;

import java.util.List;

public record CreateFeedRequest(
        String content,
        FeedType feedType,
        List<String> imageUrls,
        List<String> videoUrls
) {
}

package com.official.lockr.domain.club.feed.application.dto;

import java.util.List;

public record UpdateFeedCommand(
        String feedId,
        String userId,
        String clubId,
        String content,
        List<String> imageUrls,
        List<String> videoUrls
) {
}

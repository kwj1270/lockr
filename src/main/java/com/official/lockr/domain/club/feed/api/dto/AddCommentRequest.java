package com.official.lockr.domain.club.feed.api.dto;

import java.util.List;

public record AddCommentRequest(
        String content,
        List<String> imageUrls,
        List<String> videoUrls
) {
}

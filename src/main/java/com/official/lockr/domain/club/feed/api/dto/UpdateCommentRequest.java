package com.official.lockr.domain.club.feed.api.dto;

import java.util.List;

public record UpdateCommentRequest(
        String content,
        List<String> imageUrls,
        List<String> videoUrls
) {
}
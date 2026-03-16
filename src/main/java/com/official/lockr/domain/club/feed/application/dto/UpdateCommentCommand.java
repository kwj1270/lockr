package com.official.lockr.domain.club.feed.application.dto;

import java.util.List;

public record UpdateCommentCommand(
        String feedId,
        String commentId,
        String userId,
        String clubId,
        String content,
        List<String> imageUrls,
        List<String> videoUrls
) {
}
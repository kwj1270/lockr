package com.official.lockr.domain.club.feed.api.dto;

import com.official.lockr.domain.club.feed.application.dto.UpdateCommentCommand;

import java.util.List;

public record UpdateCommentRequest(
        String content,
        List<String> imageUrls,
        List<String> videoUrls
) {
    public UpdateCommentCommand toCommand(final String feedId, final String commentId, final String userId, final String clubId) {
        return new UpdateCommentCommand(
                feedId, commentId, userId, clubId,
                content, imageUrls, videoUrls
        );
    }
}

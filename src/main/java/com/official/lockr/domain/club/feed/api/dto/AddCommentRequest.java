package com.official.lockr.domain.club.feed.api.dto;

import com.official.lockr.domain.club.feed.application.dto.AddCommentCommand;

import java.util.List;

public record AddCommentRequest(
        String content,
        List<String> imageUrls,
        List<String> videoUrls
) {
    public AddCommentCommand toCommand(final String feedId, final String userId, final String clubId) {
        return new AddCommentCommand(feedId, userId, clubId, content, imageUrls, videoUrls);
    }
}

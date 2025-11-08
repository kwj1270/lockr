package com.official.lockr.domain.club.feed.application.dto;

public record AddCommentHeartCommand(
        String feedId,
        String commentId,
        String userId,
        String clubId
) {
}
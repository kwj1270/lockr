package com.official.lockr.domain.club.feed.application.command;

public record AddCommentHeartCommand(
        String feedId,
        String commentId,
        String userId,
        String clubId
) {
}
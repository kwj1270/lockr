package com.official.lockr.domain.shorts.application.command;

public record DeleteShortsCommentCommand(
        String shortsId,
        String commentId,
        String userId
) {
}

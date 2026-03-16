package com.official.lockr.domain.shorts.application.command;

public record AddShortsCommentCommand(
        String shortsId,
        String userId,
        String content
) {
}

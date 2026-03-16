package com.official.lockr.domain.shorts.api.dto;

import com.official.lockr.domain.shorts.application.command.AddShortsCommentCommand;

public record AddShortsCommentRequest(
        String content
) {
    public AddShortsCommentCommand toCommand(final String shortsId, final String userId) {
        return new AddShortsCommentCommand(shortsId, userId, content);
    }
}

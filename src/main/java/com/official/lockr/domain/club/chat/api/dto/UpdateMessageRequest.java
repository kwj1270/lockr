package com.official.lockr.domain.club.chat.api.dto;

import com.official.lockr.domain.club.chat.application.command.UpdateMessageCommand;

public record UpdateMessageRequest(
        String content
) {
    public UpdateMessageCommand toCommand(final String clubId, final String chatRoomId, final String chatId, final String requesterId) {
        return new UpdateMessageCommand(clubId, chatRoomId, chatId, content, requesterId);
    }
}

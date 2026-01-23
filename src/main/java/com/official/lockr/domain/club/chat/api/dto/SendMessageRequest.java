package com.official.lockr.domain.club.chat.api.dto;

import com.official.lockr.domain.club.chat.application.dto.SendMessageCommand;

public record SendMessageRequest(
        String message,
        String repliedToId
) {
    public SendMessageCommand toCommand(final String clubId, final String chatRoomId, final String senderId) {
        return new SendMessageCommand(clubId, chatRoomId, senderId, message, repliedToId);
    }
}

package com.official.lockr.domain.club.chat.application.command;

public record SendMessageCommand(
        String clubId,
        String chatRoomId,
        String senderId,
        String message,
        String repliedToId
) {
}

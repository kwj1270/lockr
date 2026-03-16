package com.official.lockr.domain.club.chat.application.command;

public record UpdateMessageCommand(
        String clubId,
        String chatRoomId,
        String chatId,
        String content,
        String requesterId
) {
}

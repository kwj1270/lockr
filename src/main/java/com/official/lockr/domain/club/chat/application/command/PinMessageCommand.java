package com.official.lockr.domain.club.chat.application.command;

public record PinMessageCommand(
        String clubId,
        String chatRoomId,
        String chatId,
        String requesterId
) {
}

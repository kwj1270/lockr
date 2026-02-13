package com.official.lockr.domain.club.chat.application.command;

public record CreateChatRoomCommand(
        String clubId,
        String name,
        String defaultChatterUserId
) {
}

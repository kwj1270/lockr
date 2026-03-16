package com.official.lockr.domain.club.chat.application.command;

public record LeaveChatRoomCommand(
        String clubId,
        String chatRoomId,
        String userId
) {
}

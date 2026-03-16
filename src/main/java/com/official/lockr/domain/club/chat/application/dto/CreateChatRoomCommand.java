package com.official.lockr.domain.club.chat.application.dto;

public record CreateChatRoomCommand(
        String clubId,
        String name
) {
}

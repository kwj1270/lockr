package com.official.lockr.domain.club.chat.application.dto;

public record SendMessageCommand(
        String clubId,
        String chatRoomId,
        String senderId,
        String senderNickname,
        String message
) {
}

package com.official.lockr.domain.club.chat.application.dto;

public record AddChatterCommand(
        String clubId,
        String chatRoomId,
        String userId
) {
}

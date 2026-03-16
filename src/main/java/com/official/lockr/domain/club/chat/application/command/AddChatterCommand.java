package com.official.lockr.domain.club.chat.application.command;

public record AddChatterCommand(
        String clubId,
        String chatRoomId,
        String userId
) {
}

package com.official.lockr.domain.club.chat.application.command;

public record RemoveChatterCommand(
        String clubId,
        String chatRoomId,
        String userId
) {
}

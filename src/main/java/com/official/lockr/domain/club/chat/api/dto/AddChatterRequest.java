package com.official.lockr.domain.club.chat.api.dto;

import com.official.lockr.domain.club.chat.application.dto.AddChatterCommand;

public record AddChatterRequest(
        String userId
) {
    public AddChatterCommand toCommand(final String clubId, final String chatRoomId) {
        return new AddChatterCommand(clubId, chatRoomId, userId);
    }
}

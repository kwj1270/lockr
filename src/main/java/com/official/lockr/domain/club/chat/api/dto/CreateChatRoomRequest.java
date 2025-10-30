package com.official.lockr.domain.club.chat.api.dto;

import com.official.lockr.domain.club.chat.application.dto.CreateChatRoomCommand;

public record CreateChatRoomRequest(
        String name,
        String creatorNickname
) {
    public CreateChatRoomCommand toCommand(final String clubId) {
        return new CreateChatRoomCommand(clubId, name);
    }
}

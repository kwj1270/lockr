package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.dto.CreateChatRoomCommand;
import com.official.lockr.domain.club.chat.domain.ChatRoom;

public interface CreateChatRoomUseCase {
    ChatRoom create(final CreateChatRoomCommand command);
}

package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.command.CreateChatRoomCommand;
import com.official.lockr.domain.club.chat.domain.ChatRoom;

public interface CreateChatRoomUseCase {
    ChatRoom create(final CreateChatRoomCommand command);
}

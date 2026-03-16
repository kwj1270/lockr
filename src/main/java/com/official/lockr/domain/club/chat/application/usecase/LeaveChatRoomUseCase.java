package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.command.LeaveChatRoomCommand;

public interface LeaveChatRoomUseCase {
    void leave(LeaveChatRoomCommand command);
}

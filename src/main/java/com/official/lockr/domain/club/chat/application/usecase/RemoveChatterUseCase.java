package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.command.RemoveChatterCommand;
import com.official.lockr.domain.club.chat.domain.ChatRoom;

public interface RemoveChatterUseCase {
    ChatRoom removeChatter(final RemoveChatterCommand command);
}

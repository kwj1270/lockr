package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.command.UpdateMessageCommand;
import com.official.lockr.domain.club.chat.domain.Chat;

public interface UpdateMessageUseCase {
    Chat update(UpdateMessageCommand command);
}

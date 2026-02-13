package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.command.SendMessageCommand;
import com.official.lockr.domain.club.chat.domain.Chat;

public interface SendMessageUseCase {
    Chat send(final SendMessageCommand command);
}

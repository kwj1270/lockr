package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.dto.SendMessageCommand;
import com.official.lockr.domain.club.chat.domain.Chat;

public interface SendMessageUseCase {
    Chat send(final SendMessageCommand command);
}

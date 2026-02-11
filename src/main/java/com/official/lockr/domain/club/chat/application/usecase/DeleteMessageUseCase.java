package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.command.DeleteMessageCommand;

public interface DeleteMessageUseCase {
    void delete(DeleteMessageCommand command);
}

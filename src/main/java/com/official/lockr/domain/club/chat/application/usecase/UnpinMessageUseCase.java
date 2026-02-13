package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.command.UnpinMessageCommand;

public interface UnpinMessageUseCase {
    void unpin(UnpinMessageCommand command);
}

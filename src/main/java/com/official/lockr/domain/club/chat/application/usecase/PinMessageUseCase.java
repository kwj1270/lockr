package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.command.PinMessageCommand;
import com.official.lockr.domain.club.chat.domain.PinnedMessage;

public interface PinMessageUseCase {
    PinnedMessage pin(PinMessageCommand command);
}

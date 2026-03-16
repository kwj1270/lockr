package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.application.dto.AddChatterCommand;
import com.official.lockr.domain.club.chat.domain.ChatRoom;

public interface AddChatterUseCase {
    ChatRoom addChatter(final AddChatterCommand command);
}

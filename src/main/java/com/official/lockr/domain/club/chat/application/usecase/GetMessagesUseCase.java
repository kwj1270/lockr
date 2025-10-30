package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.domain.Chat;

import java.util.List;

public interface GetMessagesUseCase {
    List<Chat> getMessages(final String chatRoomId, final String userId, final String lastChatId, final int limit);
}

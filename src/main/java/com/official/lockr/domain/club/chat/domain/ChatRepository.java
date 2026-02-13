package com.official.lockr.domain.club.chat.domain;

import java.util.List;
import java.util.Optional;

public interface ChatRepository {
    Chat save(Chat chat);

    Optional<Chat> findById(final String chatId);

    List<Chat> findAllByChatRoomId(final String chatRoomId);

    List<Chat> findAllByChatRoomId(final String chatRoomId, final String lastChatId, final int limit);

    List<Chat> findAllAfterChatId(final String chatRoomId, final String afterChatId, final int limit);

    void softDelete(final String chatId);
}

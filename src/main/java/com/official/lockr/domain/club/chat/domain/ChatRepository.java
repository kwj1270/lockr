package com.official.lockr.domain.club.chat.domain;

import java.util.List;

public interface ChatRepository {
    Chat save(Chat chat);

    List<Chat> findAllByChatRoomId(final String chatRoomId);

    List<Chat> findAllByChatRoomId(final String chatRoomId, final String lastChatId, final int limit);
}

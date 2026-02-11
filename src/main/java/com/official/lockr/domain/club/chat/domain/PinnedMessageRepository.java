package com.official.lockr.domain.club.chat.domain;

import java.util.List;
import java.util.Optional;

public interface PinnedMessageRepository {

    PinnedMessage save(PinnedMessage pinnedMessage);

    void deleteByChatRoomIdAndChatId(String chatRoomId, String chatId);

    Optional<PinnedMessage> findByChatRoomIdAndChatId(String chatRoomId, String chatId);

    List<PinnedMessage> findAllByChatRoomId(String chatRoomId);
}

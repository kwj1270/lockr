package com.official.lockr.domain.club.chat.domain;

import java.util.List;

public interface ChatRoomRepository {
    ChatRoom findById(final String id);

    List<ChatRoom> findAllByClubId(final String clubId);

    ChatRoom save(ChatRoom chatRoom);

    void delete(final String id);

    ChatRoom findByClubId(String clubId);
}

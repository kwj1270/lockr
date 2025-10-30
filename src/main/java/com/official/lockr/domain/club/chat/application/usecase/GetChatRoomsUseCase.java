package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.domain.ChatRoom;

import java.util.List;

public interface GetChatRoomsUseCase {
    List<ChatRoom> getChatRooms(final String clubId, final String userId);
}

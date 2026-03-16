package com.official.lockr.domain.club.chat.domain.event;

import com.official.lockr.domain.club.chat.domain.Chat;

public record ChatSseEvent(
        String chatRoomId,
        String clubId,
        ChatSseEventType type,
        Chat chat
) {
    public static ChatSseEvent chatRoomCreated(final String chatRoomId, final String clubId) {
        return new ChatSseEvent(chatRoomId, clubId, ChatSseEventType.ROOM_CREATED, null);
    }

    public static ChatSseEvent newMessage(final Chat chat, final String clubId) {
        return new ChatSseEvent(chat.getChatRoomId(), clubId, ChatSseEventType.NEW_MESSAGE, chat);
    }

    public static ChatSseEvent chatRoomUpdated(final String chatRoomId, final String clubId) {
        return new ChatSseEvent(chatRoomId, clubId, ChatSseEventType.ROOM_UPDATED, null);
    }
}

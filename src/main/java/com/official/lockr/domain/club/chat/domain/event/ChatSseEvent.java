package com.official.lockr.domain.club.chat.domain.event;

import com.official.lockr.domain.club.chat.domain.Chat;

public record ChatSseEvent(
        String chatRoomId,
        String clubId,
        ChatSseEventType type,
        Chat chat,
        String removedUserId
) {
    public static ChatSseEvent chatRoomCreated(final String chatRoomId, final String clubId) {
        return new ChatSseEvent(chatRoomId, clubId, ChatSseEventType.ROOM_CREATED, null, null);
    }

    public static ChatSseEvent newMessage(final Chat chat, final String clubId) {
        return new ChatSseEvent(chat.getChatRoomId(), clubId, ChatSseEventType.NEW_MESSAGE, chat, null);
    }

    public static ChatSseEvent chatRoomUpdated(final String chatRoomId, final String clubId) {
        return new ChatSseEvent(chatRoomId, clubId, ChatSseEventType.ROOM_UPDATED, null, null);
    }

    public static ChatSseEvent chatterLeft(final String chatRoomId, final String clubId, final String removedUserId) {
        return new ChatSseEvent(chatRoomId, clubId, ChatSseEventType.CHATTER_LEFT, null, removedUserId);
    }

    public static ChatSseEvent messageDeleted(final String chatRoomId, final String clubId, final String chatId) {
        final Chat deletedChat = new Chat(chatId, chatRoomId, null, null, null, null, null, null);
        return new ChatSseEvent(chatRoomId, clubId, ChatSseEventType.MESSAGE_DELETED, deletedChat, null);
    }

    public static ChatSseEvent messagePinned(final String chatRoomId, final String clubId, final Chat chat) {
        return new ChatSseEvent(chatRoomId, clubId, ChatSseEventType.MESSAGE_PINNED, chat, null);
    }

    public static ChatSseEvent messageUnpinned(final String chatRoomId, final String clubId, final String chatId) {
        final Chat unpinnedChat = new Chat(chatId, chatRoomId, null, null, null, null, null, null);
        return new ChatSseEvent(chatRoomId, clubId, ChatSseEventType.MESSAGE_UNPINNED, unpinnedChat, null);
    }
}

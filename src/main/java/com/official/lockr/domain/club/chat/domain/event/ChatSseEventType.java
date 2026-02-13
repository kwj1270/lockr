package com.official.lockr.domain.club.chat.domain.event;

public enum ChatSseEventType {
    ROOM_CREATED,
    ROOM_UPDATED,
    NEW_MESSAGE,
    MESSAGE_DELETED,
    MESSAGE_PINNED,
    MESSAGE_UNPINNED,
    CHATTER_JOINED,
    CHATTER_LEFT
}

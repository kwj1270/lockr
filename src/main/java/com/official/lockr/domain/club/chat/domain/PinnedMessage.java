package com.official.lockr.domain.club.chat.domain;

import java.time.LocalDateTime;

public class PinnedMessage {

    private final String id;
    private final String chatRoomId;
    private final String chatId;
    private final String pinnedBy;
    private final LocalDateTime createdAt;

    public PinnedMessage(final String id, final String chatRoomId, final String chatId,
                         final String pinnedBy, final LocalDateTime createdAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.chatId = chatId;
        this.pinnedBy = pinnedBy;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getPinnedBy() {
        return pinnedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

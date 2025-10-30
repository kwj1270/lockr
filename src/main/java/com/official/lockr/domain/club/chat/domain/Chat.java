package com.official.lockr.domain.club.chat.domain;

import com.official.lockr.domain.club.chat.domain.event.CreatedChatEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;

public class Chat extends AggregateRoot {

    private final String id;
    private final String chatRoomId;
    private final String senderId;
    private final String senderNickname;
    private final String message;
    private final LocalDateTime createdAt;

    public static Chat init(final String id, final String chatRoomId, final String senderId,
                            final String senderNickname, final String message) {
        final Chat chat = new Chat(id, chatRoomId, senderId, senderNickname, message, LocalDateTime.now());
        // clubId는 ChatEventListener에서 ChatRoom 조회를 통해 가져옴
        chat.addEvent(new CreatedChatEvent(
                chat.id,
                chat.chatRoomId,
                null, // clubId는 EventListener에서 채움
                chat.senderId,
                chat.senderNickname,
                chat.message
        ));
        return chat;
    }

    public Chat(final String id, final String chatRoomId, final String senderId,
                final String senderNickname, final String message, final LocalDateTime createdAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

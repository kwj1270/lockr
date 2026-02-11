package com.official.lockr.domain.club.chat.domain;

import com.official.lockr.domain.club.chat.domain.event.CreatedChatEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;

public class Chat extends AggregateRoot {

    private final String id;
    private final String chatRoomId;
    private final String senderId;
    private final String senderName;
    private final String message;
    private final String repliedToId;
    private final String quotedSenderName;
    private final String quotedContent;
    private final LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public static Chat init(
            final String id,
            final String chatRoomId,
            final String senderId,
            final String senderNickname,
            final String message,
            final String repliedToId,
            final String quotedSenderName,
            final String quotedContent
    ) {
        final Chat chat = new Chat(
                id, chatRoomId, senderId, senderNickname, message,
                repliedToId, quotedSenderName, quotedContent, LocalDateTime.now()
        );
        // clubId는 ChatEventListener에서 ChatRoom 조회를 통해 가져옴
        chat.addEvent(new CreatedChatEvent(
                chat.id,
                chat.chatRoomId,
                null, // clubId는 EventListener에서 채움
                chat.senderId,
                chat.senderName,
                chat.message,
                chat.repliedToId,
                chat.quotedSenderName,
                chat.quotedContent
        ));
        return chat;
    }

    public Chat(
            final String id,
            final String chatRoomId,
            final String senderId,
            final String senderName,
            final String message,
            final String repliedToId,
            final String quotedSenderName,
            final String quotedContent,
            final LocalDateTime createdAt
    ) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.repliedToId = repliedToId;
        this.quotedSenderName = quotedSenderName;
        this.quotedContent = quotedContent;
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

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public String getRepliedToId() {
        return repliedToId;
    }

    public String getQuotedSenderName() {
        return quotedSenderName;
    }

    public String getQuotedContent() {
        return quotedContent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}

package com.official.lockr.domain.club.chat.infrastructure.sse;

import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.domain.ChatRoomRepository;
import com.official.lockr.domain.club.chat.domain.event.ChatSseEvent;
import com.official.lockr.domain.club.chat.domain.event.CreatedChatEvent;
import com.official.lockr.domain.club.chat.domain.event.CreatedChatRoomEvent;
import com.official.lockr.domain.club.chat.domain.event.RemovedChatterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ChatEventListener {

    private static final Logger log = LoggerFactory.getLogger(ChatEventListener.class);

    private final SseChatEventPublisher sseChatEventPublisher;
    private final ChatRoomRepository chatRoomRepository;

    public ChatEventListener(
            final SseChatEventPublisher sseChatEventPublisher,
            final ChatRoomRepository chatRoomRepository) {
        this.sseChatEventPublisher = sseChatEventPublisher;
        this.chatRoomRepository = chatRoomRepository;
    }

    @EventListener
    public void createdChatRoomEvent(final CreatedChatRoomEvent event) {
        try {
            log.debug("Processing CreatedChatRoomEvent: chatRoomId={}, clubId={}", event.id(), event.clubId());
            sseChatEventPublisher.publish(ChatSseEvent.chatRoomUpdated(event.id(), event.clubId()));
        } catch (Exception e) {
            log.error("Failed to publish SSE for chat room creation. chatRoomId={}, clubId={}",
                    event.id(), event.clubId(), e);
        }
    }

    @EventListener
    public void removedChatterEvent(final RemovedChatterEvent event) {
        try {
            log.debug("Processing RemovedChatterEvent: chatRoomId={}, clubId={}, userId={}",
                    event.chatRoomId(), event.clubId(), event.userId());
            sseChatEventPublisher.publish(ChatSseEvent.chatterLeft(event.chatRoomId(), event.clubId(), event.userId()));
            log.info("Chatter left event published via SSE. chatRoomId={}, clubId={}, userId={}",
                    event.chatRoomId(), event.clubId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to publish SSE for chatter removal. chatRoomId={}, clubId={}, userId={}",
                    event.chatRoomId(), event.clubId(), event.userId(), e);
        }
    }

    @EventListener
    public void createdChatEvent(final CreatedChatEvent event) {
        try {
            log.debug("Processing CreatedChatEvent: chatId={}, chatRoomId={}", event.chatId(), event.chatRoomId());

            final ChatRoom chatRoom = chatRoomRepository.findById(event.chatRoomId());
            if (chatRoom == null) {
                log.warn("ChatRoom not found for chat message. chatRoomId={}, chatId={}",
                        event.chatRoomId(), event.chatId());
                return;
            }

            final Chat chat = new Chat(
                    event.chatId(),
                    event.chatRoomId(),
                    event.senderId(),
                    event.message(),
                    event.repliedToId(),
                    event.quotedSenderName(),
                    event.quotedContent(),
                    java.time.LocalDateTime.now()
            );

            sseChatEventPublisher.publish(ChatSseEvent.newMessage(chat, chatRoom.getClubId()));
            log.info("Chat message published via SSE. chatId={}, chatRoomId={}, clubId={}",
                    event.chatId(), event.chatRoomId(), chatRoom.getClubId());
        } catch (Exception e) {
            log.error("Failed to publish SSE for chat message. chatId={}, chatRoomId={}",
                    event.chatId(), event.chatRoomId(), e);
        }
    }
}

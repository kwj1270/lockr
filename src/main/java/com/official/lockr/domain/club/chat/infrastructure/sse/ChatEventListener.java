package com.official.lockr.domain.club.chat.infrastructure.sse;

import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.domain.ChatRoomRepository;
import com.official.lockr.domain.club.chat.domain.event.ChatSseEvent;
import com.official.lockr.domain.club.chat.domain.event.CreatedChatEvent;
import com.official.lockr.domain.club.chat.domain.event.CreatedChatRoomEvent;
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

    /**
     * 채팅방 생성 이벤트 처리
     */
    @EventListener
    public void createdChatRoomEvent(final CreatedChatRoomEvent event) {
        log.debug("Processing CreatedChatRoomEvent: chatRoomId={}, clubId={}", event.id(), event.clubId());
        sseChatEventPublisher.publish(ChatSseEvent.chatRoomUpdated(event.id(), event.clubId()));
    }

    /**
     * 채팅 메시지 생성 이벤트 처리
     * - ChatRoom 조회하여 clubId 획득
     * - SSE로 실시간 전송
     */
    @EventListener
    public void createdChatEvent(final CreatedChatEvent event) {
        log.debug("Processing CreatedChatEvent: chatId={}, chatRoomId={}", event.chatId(), event.chatRoomId());

        // ChatRoom 조회하여 clubId 가져오기
        final ChatRoom chatRoom = chatRoomRepository.findById(event.chatRoomId());
        if (chatRoom == null) {
            log.warn("ChatRoom not found for chat message. chatRoomId={}, chatId={}",
                    event.chatRoomId(), event.chatId());
            return;
        }

        // Chat 도메인 객체 재구성
        final Chat chat = new Chat(
                event.chatId(),
                event.chatRoomId(),
                event.senderId(),
                event.senderNickname(),
                event.message(),
                event.repliedToId(),
                event.quotedSenderName(),
                event.quotedContent(),
                java.time.LocalDateTime.now()
        );

        // SSE로 전송
        sseChatEventPublisher.publish(ChatSseEvent.newMessage(chat, chatRoom.getClubId()));
        log.info("Chat message published via SSE. chatId={}, chatRoomId={}, clubId={}",
                event.chatId(), event.chatRoomId(), chatRoom.getClubId());
    }
}

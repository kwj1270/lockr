package com.official.lockr.domain.club.chat.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

/**
 * 채팅 메시지 생성 이벤트
 * - SSE를 통해 실시간으로 클라이언트에게 전송됨
 */
public record CreatedChatEvent(
        String chatId,
        String chatRoomId,
        String clubId,
        String senderId,
        String senderNickname,
        String message,
        String repliedToId,
        String quotedSenderName,
        String quotedContent
) implements DomainEvent {
}

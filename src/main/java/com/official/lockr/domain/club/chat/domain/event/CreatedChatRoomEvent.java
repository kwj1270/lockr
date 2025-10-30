package com.official.lockr.domain.club.chat.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record CreatedChatRoomEvent(
        String id,
        String clubId
) implements DomainEvent {
}

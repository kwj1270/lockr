package com.official.lockr.domain.club.chat.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

public record RemovedChatterEvent(
        String chatRoomId,
        String clubId,
        String userId
) implements DomainEvent {
}

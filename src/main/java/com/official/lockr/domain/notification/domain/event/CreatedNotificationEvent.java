package com.official.lockr.domain.notification.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record CreatedNotificationEvent(
        String id,
        String userId,
        String clubId,
        String type,
        String title,
        LocalDateTime createdAt
) implements DomainEvent {
}

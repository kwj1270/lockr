package com.official.lockr.domain.shorts.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record UploadedShortsEvent(
        String shortsId,
        String clubId,
        String userId,
        String title,
        LocalDateTime createdAt
) implements DomainEvent {
}

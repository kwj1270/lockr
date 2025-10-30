package com.official.lockr.domain.club.club.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record FoundClubEvent(
        String id,
        String name,
        String description,
        LocalDateTime createdAt
) implements DomainEvent {
}

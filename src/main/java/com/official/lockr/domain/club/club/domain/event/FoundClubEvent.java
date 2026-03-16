package com.official.lockr.domain.club.club.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record FoundClubEvent(
        String id,
        String name,
        String sportType,
        String city,
        String district,
        String description,
        LocalDateTime createdAt
) implements DomainEvent {
}

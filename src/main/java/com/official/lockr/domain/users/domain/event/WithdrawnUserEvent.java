package com.official.lockr.domain.users.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record WithdrawnUserEvent(
        String userId,
        LocalDateTime withdrawnAt
) implements DomainEvent {
}

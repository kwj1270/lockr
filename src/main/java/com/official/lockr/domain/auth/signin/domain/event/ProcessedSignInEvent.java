package com.official.lockr.domain.auth.signin.domain.event;

import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record ProcessedSignInEvent(
        String id,
        String userId,
        String deviceId,
        String deviceName,
        String deviceOS,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) implements DomainEvent {
}

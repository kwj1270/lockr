package com.official.lockr.domain.notification.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.official.lockr.domain.notification.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String userId,
        String clubId,
        String type,
        String title,
        String content,
        JsonNode data,
        boolean isRead,
        String actionType,
        String actionUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NotificationResponse from(final Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getClubId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getContent(),
                notification.getData(),
                notification.isRead(),
                notification.getActionType(),
                notification.getActionUrl(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}

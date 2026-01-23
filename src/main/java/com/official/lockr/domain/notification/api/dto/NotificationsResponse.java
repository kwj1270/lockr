package com.official.lockr.domain.notification.api.dto;

import java.util.List;

public record NotificationsResponse(
        List<NotificationResponse> notifications
) {
}

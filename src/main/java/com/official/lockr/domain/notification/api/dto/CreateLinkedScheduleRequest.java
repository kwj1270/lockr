package com.official.lockr.domain.notification.api.dto;

public record CreateLinkedScheduleRequest(
        String notificationId,
        String sourceScheduleId
) {
}

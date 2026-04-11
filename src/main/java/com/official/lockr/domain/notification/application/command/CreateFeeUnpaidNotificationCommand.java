package com.official.lockr.domain.notification.application.command;

import java.util.List;

public record CreateFeeUnpaidNotificationCommand(
        String clubId,
        int year,
        int month,
        String sentBy,
        List<String> targetUserIds
) {
}

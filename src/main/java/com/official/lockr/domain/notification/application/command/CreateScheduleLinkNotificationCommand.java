package com.official.lockr.domain.notification.application.command;

public record CreateScheduleLinkNotificationCommand(
        String targetClubId,
        String sourceScheduleId,
        String sourceClubId,
        String sourceClubName,
        String scheduleTitle,
        String scheduleTime
) {
}

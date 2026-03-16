package com.official.lockr.domain.club.schedule.application.command;

public record CancelScheduleCommand(
        String userId,
        String clubId,
        String scheduleId
) {
    public CancelScheduleCommand {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        if (clubId == null || clubId.isBlank()) {
            throw new IllegalArgumentException("clubId must not be null or blank");
        }
        if (scheduleId == null || scheduleId.isBlank()) {
            throw new IllegalArgumentException("scheduleId must not be null or blank");
        }
    }
}

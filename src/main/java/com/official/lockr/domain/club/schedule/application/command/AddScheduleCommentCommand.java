package com.official.lockr.domain.club.schedule.application.command;

public record AddScheduleCommentCommand(
        String scheduleId,
        String userId,
        String clubId,
        String content
) {
}

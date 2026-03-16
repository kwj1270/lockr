package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.application.command.AddScheduleCommentCommand;

public record AddScheduleCommentRequest(
        String content
) {
    public AddScheduleCommentCommand toCommand(final String scheduleId, final String userId, final String clubId) {
        return new AddScheduleCommentCommand(scheduleId, userId, clubId, content);
    }
}

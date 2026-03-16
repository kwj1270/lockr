package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.application.command.UpdateScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateScheduleRequest(
        @NotBlank String title,
        String content,
        String location,
        @NotNull LocalDateTime scheduleTime,
        ScheduleDetailData detail,
        Integer minParticipants,
        int deadlineDays
) {
    public UpdateScheduleCommand toCommand(final String scheduleId, final String userId, final String clubId) {
        return new UpdateScheduleCommand(
                scheduleId, userId, clubId, title, content, location, scheduleTime, detail,
                minParticipants, deadlineDays
        );
    }
}

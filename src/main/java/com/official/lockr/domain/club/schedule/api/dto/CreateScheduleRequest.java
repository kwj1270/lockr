package com.official.lockr.domain.club.schedule.api.dto;

import com.official.lockr.domain.club.schedule.application.command.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateScheduleRequest(
        @NotBlank String title,
        String content,
        String location,
        @NotNull LocalDateTime scheduleTime,
        @NotNull ScheduleType scheduleType,
        String detail,
        Integer minParticipants,
        int deadlineDays
) {
    public CreateScheduleCommand toCommand(final String userId, final String clubId, final ScheduleDetailData parsedDetail) {
        return new CreateScheduleCommand(
                userId, clubId, title, content, location, scheduleTime, scheduleType, parsedDetail,
                minParticipants, deadlineDays
        );
    }
}

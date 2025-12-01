package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.dto.RespondToScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.AttendanceStatus;

public interface RespondToScheduleUseCase {

    default Schedule respond(
            final String scheduleId,
            final String userId,
            final String clubId,
            final AttendanceStatus status,
            final String reason
    ) {
        return this.respond(new RespondToScheduleCommand(scheduleId, userId, clubId, status, reason));
    }

    Schedule respond(final RespondToScheduleCommand command);
}

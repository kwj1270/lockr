package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.dto.UpdateScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;

import java.time.LocalDateTime;

public interface UpdateScheduleUseCase {

    default Schedule update(
            final String scheduleId, final String userId, final String clubId, final String title, final String content,
            final String location, final LocalDateTime scheduleTime, final ScheduleDetailData detail,
            final int minParticipants, final int maxParticipants, final int deadlineDays
    ) {
        return update(new UpdateScheduleCommand(
                scheduleId, userId, clubId, title, content, location, scheduleTime, detail,
                minParticipants, maxParticipants, deadlineDays
        ));
    }

    Schedule update(final UpdateScheduleCommand command);
}

package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.dto.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.Schedule;
import com.official.lockr.domain.club.schedule.domain.ScheduleType;
import com.official.lockr.domain.club.schedule.domain.vo.ScheduleDetailData;

import java.time.LocalDateTime;

public interface RegisterScheduleUseCase {

    default Schedule create(final String userId, final String clubId, final String title, final String content,
                            final String location, final LocalDateTime scheduleTime, final ScheduleType scheduleType, final ScheduleDetailData detail,
                            final int minParticipants, final int maxParticipants, final int deadlineDays
    ) {
        return create(
                new CreateScheduleCommand(userId, clubId, title, content, location, scheduleTime, scheduleType, detail,
                        minParticipants, maxParticipants, deadlineDays
                )
        );
    }

    Schedule create(final CreateScheduleCommand command);
}

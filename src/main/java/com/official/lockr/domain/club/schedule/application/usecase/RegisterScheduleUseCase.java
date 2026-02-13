package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.command.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.Schedule;

public interface RegisterScheduleUseCase {
    Schedule create(final CreateScheduleCommand command);
}

package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.command.UpdateScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.Schedule;

public interface UpdateScheduleUseCase {
    Schedule update(final UpdateScheduleCommand command);
}

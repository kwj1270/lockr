package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.command.CancelScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.Schedule;

public interface CancelScheduleUseCase {
    Schedule cancel(final CancelScheduleCommand command);
}

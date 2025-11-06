package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.dto.CreateScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.Schedule;

public interface CreateScheduleUseCase {

    Schedule create(final CreateScheduleCommand command);
}

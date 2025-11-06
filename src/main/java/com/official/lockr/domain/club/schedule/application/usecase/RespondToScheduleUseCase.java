package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.dto.RespondToScheduleCommand;
import com.official.lockr.domain.club.schedule.domain.Schedule;

public interface RespondToScheduleUseCase {

    Schedule respond(final RespondToScheduleCommand command);
}

package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.domain.Schedule;

public interface CancelScheduleUseCase {
    Schedule cancel(final String userId, final String clubId, final String scheduleId);
}

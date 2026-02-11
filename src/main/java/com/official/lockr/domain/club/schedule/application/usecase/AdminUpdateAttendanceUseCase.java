package com.official.lockr.domain.club.schedule.application.usecase;

import com.official.lockr.domain.club.schedule.application.command.AdminUpdateAttendanceCommand;

public interface AdminUpdateAttendanceUseCase {
    void update(final AdminUpdateAttendanceCommand command);
}

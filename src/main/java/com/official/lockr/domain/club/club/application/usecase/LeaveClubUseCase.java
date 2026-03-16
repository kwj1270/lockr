package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.LeaveClubCommand;

public interface LeaveClubUseCase {
    void leave(LeaveClubCommand command);
}

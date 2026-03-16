package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.AssignCoachCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface AssignCoachUseCase {
    Club assignCoach(AssignCoachCommand command);
}

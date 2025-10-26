package com.official.lockr.domain.club.club.application;

import com.official.lockr.domain.club.club.application.command.AssignManagerCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface AssignMangerUseCase {
    Club assignManager(AssignManagerCommand command);
}

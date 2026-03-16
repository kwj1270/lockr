package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.ChangeVisibilityCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface ChangeVisibilityUseCase {
    Club changeVisibility(ChangeVisibilityCommand command);
}

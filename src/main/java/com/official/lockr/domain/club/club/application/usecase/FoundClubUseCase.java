package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.FoundClubCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface FoundClubUseCase {
    Club found(final FoundClubCommand command);
}

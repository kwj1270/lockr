package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.UpdateClubCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface UpdateClubUseCase {
    Club update(UpdateClubCommand command);
}

package com.official.lockr.domain.club.stats.application.usecase;

import com.official.lockr.domain.club.stats.application.command.UpdateMatchCommand;
import com.official.lockr.domain.club.stats.domain.MatchRecord;

public interface UpdateMatchUseCase {

    MatchRecord update(UpdateMatchCommand command);
}

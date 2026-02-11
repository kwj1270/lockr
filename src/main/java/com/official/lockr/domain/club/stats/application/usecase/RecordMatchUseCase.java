package com.official.lockr.domain.club.stats.application.usecase;

import com.official.lockr.domain.club.stats.application.command.RecordMatchCommand;
import com.official.lockr.domain.club.stats.domain.MatchRecord;

public interface RecordMatchUseCase {

    MatchRecord record(RecordMatchCommand command);
}

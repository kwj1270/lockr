package com.official.lockr.domain.club.match.application;

import com.official.lockr.domain.club.match.application.command.AcceptMatchCommand;
import com.official.lockr.domain.club.match.domain.Match;

public interface AcceptMatchUseCase {
    Match accept(AcceptMatchCommand command);
}

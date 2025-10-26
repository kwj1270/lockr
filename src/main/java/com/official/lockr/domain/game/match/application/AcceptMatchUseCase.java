package com.official.lockr.domain.game.match.application;

import com.official.lockr.domain.game.match.application.command.AcceptMatchCommand;
import com.official.lockr.domain.game.match.domain.Match;

public interface AcceptMatchUseCase {
    Match accept(AcceptMatchCommand command);
}

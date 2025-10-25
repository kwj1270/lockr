package com.official.lockr.domain.game.match.application;

import com.official.lockr.domain.game.match.application.command.InviteMatchCommand;
import com.official.lockr.domain.game.match.domain.Match;

public interface InviteMatchUseCase {
    Match invite(InviteMatchCommand command);
}

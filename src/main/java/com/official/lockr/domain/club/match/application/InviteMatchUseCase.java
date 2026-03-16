package com.official.lockr.domain.club.match.application;

import com.official.lockr.domain.club.match.application.command.InviteMatchCommand;
import com.official.lockr.domain.club.match.domain.Match;

public interface InviteMatchUseCase {
    Match invite(InviteMatchCommand command);
}

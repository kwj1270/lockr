package com.official.lockr.domain.club.club.application.usecase;

import com.official.lockr.domain.club.club.application.command.ChangeJoinMethodCommand;
import com.official.lockr.domain.club.club.domain.Club;

public interface ChangeJoinMethodUseCase {
    Club changeJoinMethod(ChangeJoinMethodCommand command);
}

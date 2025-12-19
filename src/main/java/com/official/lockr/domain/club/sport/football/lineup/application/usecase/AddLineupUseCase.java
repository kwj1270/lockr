package com.official.lockr.domain.club.sport.football.lineup.application.usecase;

import com.official.lockr.domain.club.sport.football.lineup.application.command.AddLineupsCommand;
import com.official.lockr.domain.club.sport.football.lineup.domain.Lineup;

import java.util.List;

public interface AddLineupUseCase {
    List<Lineup> addAll(AddLineupsCommand command);
}

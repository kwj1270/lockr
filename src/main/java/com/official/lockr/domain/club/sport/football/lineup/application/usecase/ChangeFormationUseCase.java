package com.official.lockr.domain.club.sport.football.lineup.application.usecase;

import com.official.lockr.domain.club.sport.football.lineup.domain.Lineup;

public interface ChangeFormationUseCase {
    Lineup changeFormation(final String clubId, final String lineupId, final String formation);
}

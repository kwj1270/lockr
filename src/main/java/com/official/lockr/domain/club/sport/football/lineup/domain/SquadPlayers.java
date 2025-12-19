package com.official.lockr.domain.club.sport.football.lineup.domain;

import com.official.lockr.domain.club.sport.football.squad.domain.Squad;
import com.official.lockr.domain.club.sport.football.squad.domain.SquadPlayer;
import com.official.lockr.domain.club.sport.football.squad.domain.SquadRepository;

public interface SquadPlayers extends SquadRepository {

    default SquadPlayer findByUserId(final String clubId, final String userId) {
        final Squad squad = this.findByClubId(clubId);
        return squad.findByUserId(userId);
    }
}

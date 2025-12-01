package com.official.lockr.domain.club.lineup.tacticalboard.infrastructure;

import com.official.lockr.domain.club.lineup.tacticalboard.domain.SquadPlayer;
import com.official.lockr.domain.club.lineup.tacticalboard.domain.SquadPlayers;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.SquadPlayersDao;
import org.springframework.stereotype.Repository;

import static org.jooq.generated.tables.SquadPlayersJOOQEntity.SQUAD_PLAYERS;

@Repository
public class JOOQSquadPlayersRepository implements SquadPlayers {

    private final SquadPlayersDao squadPlayersDao;

    public JOOQSquadPlayersRepository(final Configuration configuration) {
        this.squadPlayersDao = new SquadPlayersDao(configuration);
    }

    @Nullable
    @Override
    public SquadPlayer findByPlayerId(final String playerId) {
        return squadPlayersDao.ctx()
                .select(SQUAD_PLAYERS.ID)
                .from(SQUAD_PLAYERS)
                .where(SQUAD_PLAYERS.ID.eq(playerId))
                .fetchOptional()
                .map(record -> new SquadPlayer(record.value1()))
                .orElse(null);
    }
}

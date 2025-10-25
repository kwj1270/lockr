package com.official.lockr.domain.club.squad.domain.board;

import java.util.List;

public interface TacticalBoardRepository {
    TacticalBoard findById(final String id);

    List<TacticalBoard> findAllBySquadId(final String squadId);

    TacticalBoard save(TacticalBoard tacticalBoard);

    void delete(final String id);
}

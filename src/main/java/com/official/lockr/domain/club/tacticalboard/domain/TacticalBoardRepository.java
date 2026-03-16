package com.official.lockr.domain.club.tacticalboard.domain;

import java.util.List;

public interface TacticalBoardRepository {
    TacticalBoard findById(final String id);

    List<TacticalBoard> findAllByClubId(final String clubId);

    TacticalBoard save(TacticalBoard tacticalBoard);

    void delete(final String id);
}

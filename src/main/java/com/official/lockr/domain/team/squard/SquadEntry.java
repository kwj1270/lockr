package com.official.lockr.domain.team.squard;

import com.official.lockr.domain.team.player.Position;

public class SquadEntry {

    private final String squadId;
    private final String playerId;
    private final Position position;

    public SquadEntry(final String squadId, final String playerId, final Position position) {
        this.squadId = squadId;
        this.playerId = playerId;
        this.position = position;
    }
}

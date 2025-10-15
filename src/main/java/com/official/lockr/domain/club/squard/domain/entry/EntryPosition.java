package com.official.lockr.domain.club.squard.domain.entry;

import com.official.lockr.domain.club.common.Position;

public class EntryPosition {

    private final String playerId;
    private final int x;
    private final int y;
    private final Position position;

    public EntryPosition(final String playerId, final int x, final int y, final Position position) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.position = position;
    }
}

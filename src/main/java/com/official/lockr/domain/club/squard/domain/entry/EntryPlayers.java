package com.official.lockr.domain.club.squard.domain.entry;

import com.official.lockr.domain.club.common.Position;

public class EntryPlayers {

    private final String entryId;
    private final String playerId;
    private final EntryType entryType;
    private final Position position;

    public EntryPlayers(final String entryId, final String playerId,
                        final EntryType entryType,
                        final Position position) {
        this.entryId = entryId;
        this.playerId = playerId;
        this.entryType = entryType;
        this.position = position;
    }
}

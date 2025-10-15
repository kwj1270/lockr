package com.official.lockr.domain.club.squard.domain.entry;

import com.official.lockr.domain.club.common.Position;

public class EntryPlayer {

    private final String entryId;
    private final String playerId;
    private final String profileImage;
    private final String name;
    private final Position position;
    private final EntryType entryType;

    public EntryPlayer(final String entryId, final String playerId, final String name, final String profileImage, final EntryType entryType, final Position position) {
        this.entryId = entryId;
        this.playerId = playerId;
        this.name = name;
        this.profileImage = profileImage;
        this.entryType = entryType;
        this.position = position;
    }
}

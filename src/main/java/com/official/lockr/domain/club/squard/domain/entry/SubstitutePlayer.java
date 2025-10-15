package com.official.lockr.domain.club.squard.domain.entry;

import com.official.lockr.domain.club.common.Position;

import java.util.Objects;

public class SubstitutePlayer extends EntryPlayer {

    private final Position position;

    public SubstitutePlayer(final String entryId,
                            final String playerId,
                            final Position position) {
        super(entryId, playerId);
        this.position = Objects.requireNonNull(position, "Substitute player must have position");
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.SUBSTITUTE;
    }

    public Position getPosition() {
        return position;
    }
}

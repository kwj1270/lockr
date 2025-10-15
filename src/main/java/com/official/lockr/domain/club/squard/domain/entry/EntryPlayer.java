package com.official.lockr.domain.club.squard.domain.entry;

import java.util.Objects;

public abstract class EntryPlayer {

    protected final String entryId;
    protected final String playerId;

    protected EntryPlayer(final String entryId,
                          final String playerId) {
        this.entryId = entryId;
        this.playerId = playerId;
    }

    public String getEntryId() {
        return entryId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public abstract EntryType getEntryType();

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final EntryPlayer that = (EntryPlayer) o;
        return Objects.equals(getPlayerId(), that.getPlayerId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPlayerId());
    }
}

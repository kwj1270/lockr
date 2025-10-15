package com.official.lockr.domain.club.squard.domain.entry;

import java.util.Objects;

public class StartingPlayer extends EntryPlayer {

    private final EntryPosition entryPosition;
    private final Location location;
    private boolean isCaptain;

    public StartingPlayer(final String entryId,
                          final String playerId,
                          final EntryPosition entryPosition,
                          final Location location,
                          final boolean isCaptain) {
        super(entryId, playerId);
        this.entryPosition = Objects.requireNonNull(entryPosition, "Starting player must have entry position");
        this.location = Objects.requireNonNull(location, "Starting player must have location");
        this.isCaptain = isCaptain;
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.STARTING;
    }

    public EntryPosition getEntryPosition() {
        return entryPosition;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isCaptain() {
        return isCaptain;
    }

    public void assignCaptain() {
        this.isCaptain = true;
    }

    public void releaseCaptain() {
        this.isCaptain = false;
    }

    public boolean isGk() {
        return entryPosition.isGK();
    }

    public boolean isStarting() {
        return true;
    }
}

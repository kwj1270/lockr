package com.official.lockr.domain.club.sqaud.domain.board.player;

import com.official.lockr.domain.club.common.Position;

import java.util.Objects;

public class FieldPlayer extends TacticalBoardPlayer {

    private Position position;
    private Location location;
    private boolean isCaptain;

    public FieldPlayer(final String playerId,
                       final Position position,
                       final Location location,
                       final boolean isCaptain) {
        super(playerId);
        this.position = Objects.requireNonNull(position, "Starting player must have entry position");
        this.location = Objects.requireNonNull(location, "Starting player must have location");
        this.isCaptain = isCaptain;
    }

    @Override
    public TacticalBoardPlayerType getEntryType() {
        return TacticalBoardPlayerType.FIELD;
    }

    public Position getPosition() {
        return position;
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
        return position.isGK();
    }

    public void movePosition(final int x, final int y) {
        this.position = Position.calculate(x, y);
        this.location = new Location(x, y);
    }

    public boolean conflictLocation(final Location location) {
        return this.location.equals(location);
    }
}

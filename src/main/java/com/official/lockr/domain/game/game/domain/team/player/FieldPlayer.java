package com.official.lockr.domain.game.game.domain.team.player;

import com.official.lockr.domain.game.common.Cards;
import com.official.lockr.domain.game.common.Location;
import com.official.lockr.domain.game.common.Position;

import java.util.Objects;

public class FieldPlayer extends Player {

    private final Position position;
    private final Location location;
    private boolean isCaptain;

    public FieldPlayer(final String playerId,
                       final String playerName,
                       final int backNumber,
                       final Cards cards,
                       final Position position,
                       final Location location,
                       final boolean isCaptain
    ) {
        super(playerId, playerName, backNumber, cards);
        this.position = Objects.requireNonNull(position, "Starting player must have entry position");
        this.location = Objects.requireNonNull(location, "Starting player must have location");
        this.isCaptain = isCaptain;
    }

    @Override
    public PlayerType getEntryType() {
        return PlayerType.FIELD;
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

    public boolean isStarting() {
        return true;
    }

    public static FieldPlayer fromBench(
            final BenchPlayer benchPlayer,
            final Position entryPosition,
            final Location location
    ) {
        return new FieldPlayer(
                benchPlayer.getPlayerId(),
                benchPlayer.getPlayerName(),
                benchPlayer.getBackNumber(),
                new Cards(),
                entryPosition,
                location,
                false
        );
    }

    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }
}

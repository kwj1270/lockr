package com.official.lockr.domain.club.lineup.tacticalboard.domain.entry.player;

import com.official.lockr.global.vo.Position;

import java.util.Objects;

public class BenchPlayer extends Player {

    private final Position position;

    public BenchPlayer(final String playerId,
                       final Position position) {
        super(playerId);
        this.position = Objects.requireNonNull(position, "Substitute player must have position");
    }

    @Override
    public PlayerType getEntryType() {
        return PlayerType.BENCH;
    }

    public Position getPosition() {
        return position;
    }
}

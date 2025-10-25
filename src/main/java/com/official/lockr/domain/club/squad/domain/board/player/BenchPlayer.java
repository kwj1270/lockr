package com.official.lockr.domain.club.squad.domain.board.player;

import com.official.lockr.domain.club.common.Position;

import java.util.Objects;

public class BenchPlayer extends TacticalBoardPlayer {

    private final Position position;

    public BenchPlayer(final String playerId,
                       final Position position) {
        super(playerId);
        this.position = Objects.requireNonNull(position, "Substitute player must have position");
    }

    @Override
    public TacticalBoardPlayerType getEntryType() {
        return TacticalBoardPlayerType.BENCH;
    }

    public Position getPosition() {
        return position;
    }
}

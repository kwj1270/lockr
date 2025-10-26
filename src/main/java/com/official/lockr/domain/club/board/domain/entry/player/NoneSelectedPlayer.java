package com.official.lockr.domain.club.board.domain.entry.player;

import com.official.lockr.domain.club.common.Position;

public class NoneSelectedPlayer extends Player {

    private final Position position;

    public NoneSelectedPlayer(final String squadPlayerId,
                              final Position position
    ) {
        super(squadPlayerId);
        this.position = position;
    }

    @Override
    public PlayerType getEntryType() {
        return PlayerType.NONE_SELECTED;
    }

    public Position getPosition() {
        return position;
    }
}

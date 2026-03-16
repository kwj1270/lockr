package com.official.lockr.domain.club.tacticalboard.domain.entry.player;

import com.official.lockr.global.vo.Position;

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

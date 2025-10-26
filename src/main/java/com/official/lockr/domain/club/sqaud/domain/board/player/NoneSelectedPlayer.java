package com.official.lockr.domain.club.sqaud.domain.board.player;

import com.official.lockr.domain.club.common.Position;

public class NoneSelectedPlayer extends TacticalBoardPlayer {

    private final Position position;

    public NoneSelectedPlayer(final String squadPlayerId,
                              final Position position
    ) {
        super(squadPlayerId);
        this.position = position;
    }

    @Override
    public TacticalBoardPlayerType getEntryType() {
        return TacticalBoardPlayerType.NONE_SELECTED;
    }

    public Position getPosition() {
        return position;
    }
}

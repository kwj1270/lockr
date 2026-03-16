package com.official.lockr.domain.club.sqaud.domain.board.player;

import java.util.Objects;

public abstract class TacticalBoardPlayer {

    protected final String squadPlayerId;

    protected TacticalBoardPlayer(final String squadPlayerId) {
        this.squadPlayerId = squadPlayerId;
    }

    public boolean isSame(final String squadPlayerId) {
        return this.squadPlayerId.equals(squadPlayerId);
    }

    public String getSquadPlayerId() {
        return squadPlayerId;
    }

    public abstract TacticalBoardPlayerType getEntryType();

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final TacticalBoardPlayer that = (TacticalBoardPlayer) o;
        return Objects.equals(getSquadPlayerId(), that.getSquadPlayerId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSquadPlayerId());
    }


}

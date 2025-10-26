package com.official.lockr.domain.club.board.domain.entry.player;

import java.util.Objects;

public abstract class Player {

    protected final String squadPlayerId;

    protected Player(final String squadPlayerId) {
        this.squadPlayerId = squadPlayerId;
    }

    public boolean isSame(final String squadPlayerId) {
        return this.squadPlayerId.equals(squadPlayerId);
    }

    public String getSquadPlayerId() {
        return squadPlayerId;
    }

    public abstract PlayerType getEntryType();

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Player that = (Player) o;
        return Objects.equals(getSquadPlayerId(), that.getSquadPlayerId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getSquadPlayerId());
    }


}

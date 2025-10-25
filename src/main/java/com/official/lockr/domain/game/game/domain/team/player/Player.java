package com.official.lockr.domain.game.game.domain.team.player;

import com.official.lockr.domain.game.common.Card;
import com.official.lockr.domain.game.common.Cards;

import java.util.Objects;

public abstract class Player {

    protected final String playerId;
    protected final String playerName;
    protected final int backNumber;
    protected Cards cards;

    protected Player(final String playerId,
                     final String playerName,
                     final int backNumber,
                     final Cards cards
    ) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.backNumber = backNumber;
        this.cards = cards;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public abstract PlayerType getEntryType();

    public boolean isStarting() {
        return getEntryType() == PlayerType.FIELD;
    }

    public boolean isSubstitute() {
        return getEntryType() == PlayerType.BENCH;
    }

    public int getBackNumber() {
        return backNumber;
    }

    public boolean hasTwoYellowCard() {
        return cards.hasTwoYellowCard();
    }

    public Cards getCards() {
        return cards;
    }

    public void addCard(final Card card) {
        this.cards = cards.add(card);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Player that = (Player) o;
        return Objects.equals(getPlayerId(), that.getPlayerId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPlayerId());
    }

}

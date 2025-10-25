package com.official.lockr.domain.game.common;

import java.util.ArrayList;
import java.util.List;

public class Cards {
    private List<Card> cards;

    public Cards() {
        this(new ArrayList<>());
    }

    public Cards(final List<Card> cards) {
        this.cards = cards;
    }

    public Cards add(final Card card) {
        cards.add(card);
        return new Cards(new ArrayList<>(cards));
    }

    public boolean hasTwoYellowCard() {
        final long yellowCard = cards.stream()
                .filter(it -> it instanceof YellowCard)
                .count();
        return yellowCard >= 2;
    }
}

package com.official.lockr.domain.game.common;

public record Score(int value) {

    public Score {
        if (value < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
    }

    public Score() {
        this(0);
    }

    public Score addGoal() {
        return new Score(this.value + 1);
    }
}

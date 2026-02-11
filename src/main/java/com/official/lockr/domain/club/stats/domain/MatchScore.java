package com.official.lockr.domain.club.stats.domain;

public record MatchScore(int ourScore, int opponentScore) {

    public MatchScore {
        if (ourScore < 0 || opponentScore < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
    }
}

package com.official.lockr.domain.club.stats.domain;

public enum MatchResult {
    WIN,
    DRAW,
    LOSE;

    public static MatchResult from(final MatchScore score) {
        if (score.ourScore() > score.opponentScore()) {
            return WIN;
        } else if (score.ourScore() < score.opponentScore()) {
            return LOSE;
        } else {
            return DRAW;
        }
    }
}

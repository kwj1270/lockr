package com.official.lockr.domain.club.stats.api.dto;

public record PlayerComparisonResponse(
        String playerId,
        String playerName,
        PlayerComparisonStats stats
) {

    public record PlayerComparisonStats(
            int appearances,
            int goals,
            int assists,
            int momCount,
            double goalsPerGame,
            double assistsPerGame,
            double contributionRate
    ) {}
}

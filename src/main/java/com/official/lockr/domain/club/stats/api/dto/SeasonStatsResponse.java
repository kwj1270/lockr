package com.official.lockr.domain.club.stats.api.dto;

import java.util.List;

public record SeasonStatsResponse(
        String clubId,
        String season,
        int totalMatches,
        int wins,
        int draws,
        int losses,
        double winRate,
        int goalsScored,
        int goalsConceded,
        int goalDifference,
        List<String> recentForm
) {
}

package com.official.lockr.domain.club.stats.api.dto;

import java.util.List;

public record PlayerStatsResponse(
        String clubId,
        String playerId,
        String playerName,
        int appearances,
        int totalGoals,
        int totalAssists,
        int momCount,
        double attendanceRate,
        List<String> recentForm
) {
}

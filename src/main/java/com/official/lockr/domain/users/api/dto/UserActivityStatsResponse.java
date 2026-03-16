package com.official.lockr.domain.users.api.dto;

public record UserActivityStatsResponse(
        int totalMatches,
        int totalGoals,
        int totalAssists,
        double attendanceRate,
        int momCount
) {
}

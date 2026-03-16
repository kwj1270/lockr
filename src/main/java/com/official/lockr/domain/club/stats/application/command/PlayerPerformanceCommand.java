package com.official.lockr.domain.club.stats.application.command;

public record PlayerPerformanceCommand(
        String userId,
        int goals,
        int assists,
        boolean isMom,
        Integer minutesPlayed
) {
}

package com.official.lockr.domain.club.stats.application.command;

import java.time.LocalDate;
import java.util.List;

public record UpdateMatchCommand(
        String recordId,
        String clubId,
        String userId,
        LocalDate matchDate,
        String opponentName,
        int ourScore,
        int opponentScore,
        List<PlayerPerformanceCommand> playerPerformances
) {
}

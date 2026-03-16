package com.official.lockr.domain.club.stats.application.command;

import java.time.LocalDate;
import java.util.List;

public record RecordMatchCommand(
        String clubId,
        String scheduleId,
        LocalDate matchDate,
        String opponentName,
        int ourScore,
        int opponentScore,
        String recordedBy,
        String season,
        List<PlayerPerformanceCommand> playerPerformances
) {
}

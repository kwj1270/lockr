package com.official.lockr.domain.club.stats.api.dto;

import com.official.lockr.domain.club.stats.application.command.PlayerPerformanceCommand;
import com.official.lockr.domain.club.stats.application.command.RecordMatchCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record RecordMatchRequest(
        String scheduleId,
        @NotNull LocalDate matchDate,
        @NotBlank String opponentName,
        int ourScore,
        int opponentScore,
        String season,
        List<PlayerPerformanceRequest> playerPerformances
) {

    public record PlayerPerformanceRequest(
            String userId,
            int goals,
            int assists,
            boolean isMom,
            Integer minutesPlayed
    ) {

        public PlayerPerformanceCommand toCommand() {
            return new PlayerPerformanceCommand(
                    userId, goals, assists, isMom, minutesPlayed
            );
        }
    }

    public RecordMatchCommand toCommand(final String clubId, final String recordedBy) {
        final String effectiveSeason = (season != null && !season.isBlank())
                ? season
                : String.valueOf(matchDate.getYear());
        return new RecordMatchCommand(
                clubId,
                scheduleId,
                matchDate,
                opponentName,
                ourScore,
                opponentScore,
                recordedBy,
                effectiveSeason,
                playerPerformances != null
                        ? playerPerformances.stream().map(PlayerPerformanceRequest::toCommand).toList()
                        : List.of()
        );
    }
}

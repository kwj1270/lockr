package com.official.lockr.domain.club.stats.api.dto;

import com.official.lockr.domain.club.stats.application.command.PlayerPerformanceCommand;
import com.official.lockr.domain.club.stats.application.command.UpdateMatchCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record UpdateMatchRequest(
        @NotNull LocalDate matchDate,
        @NotBlank String opponentName,
        int ourScore,
        int opponentScore,
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

    public UpdateMatchCommand toCommand(final String recordId, final String clubId, final String userId) {
        return new UpdateMatchCommand(
                recordId,
                clubId,
                userId,
                matchDate,
                opponentName,
                ourScore,
                opponentScore,
                playerPerformances != null
                        ? playerPerformances.stream().map(PlayerPerformanceRequest::toCommand).toList()
                        : List.of()
        );
    }
}

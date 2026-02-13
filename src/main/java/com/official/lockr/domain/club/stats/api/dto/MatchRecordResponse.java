package com.official.lockr.domain.club.stats.api.dto;

import com.official.lockr.domain.club.stats.domain.MatchRecord;
import com.official.lockr.domain.club.stats.domain.MatchResult;
import com.official.lockr.domain.club.stats.domain.PlayerPerformance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MatchRecordResponse(
        String id,
        String clubId,
        String scheduleId,
        LocalDate matchDate,
        String opponentName,
        int ourScore,
        int opponentScore,
        MatchResult result,
        String season,
        List<PlayerPerformanceResponse> playerPerformances,
        LocalDateTime createdAt,
        String recordedBy
) {

    public record PlayerPerformanceResponse(
            String id,
            String userId,
            String playerName,
            int goals,
            int assists,
            boolean isMom,
            Integer minutesPlayed
    ) {
        public static PlayerPerformanceResponse from(final PlayerPerformance perf) {
            return new PlayerPerformanceResponse(
                    perf.getId(),
                    perf.getUserId(),
                    "",
                    perf.getGoals(),
                    perf.getAssists(),
                    perf.isMom(),
                    perf.getMinutesPlayed()
            );
        }
    }

    public static MatchRecordResponse from(final MatchRecord record) {
        return new MatchRecordResponse(
                record.getId(),
                record.getClubId(),
                record.getScheduleId(),
                record.getMatchDate(),
                record.getOpponentName(),
                record.getScore().ourScore(),
                record.getScore().opponentScore(),
                record.getResult(),
                record.getSeason(),
                record.getPlayerPerformances().stream()
                        .map(PlayerPerformanceResponse::from)
                        .toList(),
                record.getCreatedAt(),
                record.getRecordedBy()
        );
    }
}

package com.official.lockr.domain.club.stats.api.dto;

import java.util.List;

public record RankingsResponse(
        List<RankedPlayer> goals,
        List<RankedPlayer> assists,
        List<RankedPlayer> mom,
        RankedPlayer myGoalRank,
        RankedPlayer myAssistRank,
        RankedPlayer myMomRank
) {

    public record RankedPlayer(
            String playerId,
            String playerName,
            int value,
            int rank
    ) {}
}

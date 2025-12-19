package com.official.lockr.domain.club.sport.football.lineup.api.dto;

import java.util.List;

public record LineupsResponse(
        List<LineupResponse> lineups,
        List<SquadPlayerResponse> squadPlayers
) {
}

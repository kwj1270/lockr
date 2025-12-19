package com.official.lockr.domain.club.sport.football.lineup.api.dto;

import java.util.List;

public record LineupResponse(
        String id,
        String name,
        String formation,
        List<SlotResponse> starters,
        List<SlotResponse> substitutes
) {
}

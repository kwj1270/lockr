package com.official.lockr.domain.club.sport.football.lineup.api.dto;

public record SquadPlayerResponse(
        String id,
        String name,
        Integer number,
        String position,
        String role
) {
}

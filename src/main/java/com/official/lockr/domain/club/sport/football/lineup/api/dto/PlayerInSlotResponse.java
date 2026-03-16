package com.official.lockr.domain.club.sport.football.lineup.api.dto;

public record PlayerInSlotResponse(
        String id,
        String name,
        Integer backNumber,
        String position
) {
}

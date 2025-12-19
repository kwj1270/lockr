package com.official.lockr.domain.club.sport.football.lineup.api.dto;

public record AssignSlotRequest(
        String slotType,
        int slotIndex,
        String squadPlayerId
) {
}
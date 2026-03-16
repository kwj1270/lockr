package com.official.lockr.domain.club.sport.football.lineup.api.dto;

public record RemoveSlotRequest(
        String slotType,
        int slotIndex
) {
}

package com.official.lockr.domain.club.sport.football.lineup.application.command;

public record RemoveSlotCommand(
        String clubId,
        String lineupId,
        String slotType,
        int slotIndex
) {
}

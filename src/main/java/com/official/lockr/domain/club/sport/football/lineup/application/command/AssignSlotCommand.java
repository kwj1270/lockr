package com.official.lockr.domain.club.sport.football.lineup.application.command;

public record AssignSlotCommand(
        String clubId,
        String lineupId,
        String slotType,
        int slotIndex,
        String squadPlayerId
) {
}

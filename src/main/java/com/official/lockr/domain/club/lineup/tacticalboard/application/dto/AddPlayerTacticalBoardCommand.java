package com.official.lockr.domain.club.lineup.tacticalboard.application.dto;

public record AddPlayerTacticalBoardCommand(
        String clubId,
        String tacticalBoardId,
        String coachUserId,
        String playerId,
        String playerType,
        int x,
        int y
) {
}

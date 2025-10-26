package com.official.lockr.domain.club.board.application.dto;

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

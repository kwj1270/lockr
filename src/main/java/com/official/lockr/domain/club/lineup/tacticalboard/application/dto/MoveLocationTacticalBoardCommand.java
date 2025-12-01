package com.official.lockr.domain.club.lineup.tacticalboard.application.dto;

public record MoveLocationTacticalBoardCommand(
        String clubId,
        String tacticalBoardId,
        String coachUserId,
        String playerId,
        int x,
        int y
) {
}

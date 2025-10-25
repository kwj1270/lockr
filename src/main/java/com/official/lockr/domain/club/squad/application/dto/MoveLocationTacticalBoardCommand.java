package com.official.lockr.domain.club.squad.application.dto;

public record MoveLocationTacticalBoardCommand(
        String tacticalBoardId,
        String squadId,
        String staffUserId,
        String squadPlayerId,
        int x,
        int y
) {
}

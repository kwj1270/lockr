package com.official.lockr.domain.club.sqaud.application.dto;

public record MoveLocationTacticalBoardCommand(
        String tacticalBoardId,
        String squadId,
        String staffUserId,
        String squadPlayerId,
        int x,
        int y
) {
}

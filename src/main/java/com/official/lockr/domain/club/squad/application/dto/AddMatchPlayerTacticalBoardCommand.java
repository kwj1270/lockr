package com.official.lockr.domain.club.squad.application.dto;

public record AddMatchPlayerTacticalBoardCommand(
        String staffUserId,
        String squadId,
        String tacticalBoardId,
        String squadPlayerId,
        String tacticalBoardPlayerType,
        int x,
        int y
) {
}

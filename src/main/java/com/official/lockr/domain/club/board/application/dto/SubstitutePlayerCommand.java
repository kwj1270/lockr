package com.official.lockr.domain.club.board.application.dto;

public record SubstitutePlayerCommand(
        String clubId,
        String tacticalBoardId,
        String coachUserId,
        String outPlayerId,
        String outPlayerType,
        String inPlayerId,
        String inPlayerType
) {
}

package com.official.lockr.domain.club.sqaud.application.dto;

public record SubstitutePlayerCommand(
        String tacticalBoardId,
        String staffUserId,
        String squadId,
        String outPlayerId,
        String outPlayerType,
        String inPlayerId,
        String inPlayerType
) {
}

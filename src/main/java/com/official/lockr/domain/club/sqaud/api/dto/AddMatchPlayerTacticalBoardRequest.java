package com.official.lockr.domain.club.sqaud.api.dto;

import com.official.lockr.domain.club.sqaud.application.dto.AddMatchPlayerTacticalBoardCommand;

public record AddMatchPlayerTacticalBoardRequest(
        String squadPlayerId,
        String squadPlayerType,
        int x,
        int y
) {
    public AddMatchPlayerTacticalBoardCommand toCommand(
            final String staffUserId,
            final String squadId,
            final String tacticalBoardId
    ) {
        return new AddMatchPlayerTacticalBoardCommand(
                staffUserId,
                squadId,
                tacticalBoardId,
                squadPlayerId,
                squadPlayerType,
                x,
                y
        );
    }
}

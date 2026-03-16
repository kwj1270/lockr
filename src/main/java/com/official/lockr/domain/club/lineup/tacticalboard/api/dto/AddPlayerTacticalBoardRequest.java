package com.official.lockr.domain.club.lineup.tacticalboard.api.dto;

import com.official.lockr.domain.club.lineup.tacticalboard.application.dto.AddPlayerTacticalBoardCommand;

public record AddPlayerTacticalBoardRequest(
        String playerId,
        String playerType,
        int x,
        int y
) {
    public AddPlayerTacticalBoardCommand toCommand(
            final String clubId,
            final String tacticalBoardId,
            final String coachUserId
    ) {
        return new AddPlayerTacticalBoardCommand(
                clubId,
                tacticalBoardId,
                coachUserId,
                playerId,
                playerType,
                x,
                y
        );
    }
}

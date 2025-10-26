package com.official.lockr.domain.club.board.api.dto;

import com.official.lockr.domain.club.board.application.dto.MoveLocationTacticalBoardCommand;

public record MoveLocationTacticalBoardRequest(
        String playerId,
        int x,
        int y
) {
    public MoveLocationTacticalBoardCommand toCommand(final String clubId, final String tacticalBoardId, final String coachUserId) {
        return new MoveLocationTacticalBoardCommand(
                clubId,
                tacticalBoardId,
                coachUserId,
                playerId,
                x,
                y
        );
    }
}

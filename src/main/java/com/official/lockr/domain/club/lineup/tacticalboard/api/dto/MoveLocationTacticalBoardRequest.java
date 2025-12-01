package com.official.lockr.domain.club.lineup.tacticalboard.api.dto;

import com.official.lockr.domain.club.lineup.tacticalboard.application.dto.MoveLocationTacticalBoardCommand;

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

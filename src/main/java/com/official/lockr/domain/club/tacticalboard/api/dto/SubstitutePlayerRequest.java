package com.official.lockr.domain.club.tacticalboard.api.dto;

import com.official.lockr.domain.club.tacticalboard.application.dto.SubstitutePlayerCommand;

public record SubstitutePlayerRequest(
        String outPlayerId,
        String outPlayerType,
        String inPlayerId,
        String inPlayerType
) {
    public SubstitutePlayerCommand toCommand(final String clubId, final String tacticalBoardId, final String coachUserId) {
        return new SubstitutePlayerCommand(clubId, tacticalBoardId, coachUserId, outPlayerId, outPlayerType, inPlayerId, inPlayerType);
    }
}

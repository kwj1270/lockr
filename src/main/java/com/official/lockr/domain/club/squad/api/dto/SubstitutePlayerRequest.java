package com.official.lockr.domain.club.squad.api.dto;

import com.official.lockr.domain.club.squad.application.dto.SubstitutePlayerCommand;

public record SubstitutePlayerRequest(
        String outPlayerId,
        String outPlayerType,
        String inPlayerId,
        String inPlayerType
) {
    public SubstitutePlayerCommand toCommand(final String userId, final String squadId, final String tacticalBoardId) {
        return new SubstitutePlayerCommand(tacticalBoardId, squadId, userId, outPlayerId, outPlayerType, inPlayerId, inPlayerType);
    }
}

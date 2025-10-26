package com.official.lockr.domain.game.match.api.dto;

import com.official.lockr.domain.game.match.application.command.AcceptMatchCommand;

public record AcceptMatchRequest(
        String teamId,
        boolean accept
) {
    public AcceptMatchCommand toCommand(final String matchProposeId, final String userId) {
        return new AcceptMatchCommand(matchProposeId, userId, teamId, accept);
    }
}

package com.official.lockr.domain.club.match.api.dto;

import com.official.lockr.domain.club.match.application.command.AcceptMatchCommand;

public record AcceptMatchRequest(
        String clubId,
        boolean accept
) {
    public AcceptMatchCommand toCommand(final String matchProposeId, final String userId) {
        return new AcceptMatchCommand(matchProposeId, userId, clubId, accept);
    }
}

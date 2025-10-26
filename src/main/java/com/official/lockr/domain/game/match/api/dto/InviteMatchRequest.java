package com.official.lockr.domain.game.match.api.dto;

import com.official.lockr.domain.game.match.application.command.InviteMatchCommand;

import java.time.LocalDateTime;

public record InviteMatchRequest(
        String homeTeamId,
        String awayTeamId,
        LocalDateTime matchDateTime,
        String location
) {
    public InviteMatchCommand toCommand(final String homeProposeUserId) {
        return new InviteMatchCommand(
                homeProposeUserId, homeTeamId, awayTeamId, matchDateTime, location
        );
    }
}

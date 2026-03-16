package com.official.lockr.domain.game.match.application.command;

import java.time.LocalDateTime;

public record InviteMatchCommand(
        String homeProposeUserId,
        String homeTeamId,
        String awayTeamId,
        LocalDateTime matchDateTime,
        String location
) {
}

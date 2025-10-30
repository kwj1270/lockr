package com.official.lockr.domain.club.match.application.command;

import java.time.LocalDateTime;

public record InviteMatchCommand(
        String homeClubId,
        String homeClubManagerUserId,
        String awayClubId,
        LocalDateTime matchDateTime,
        String location
) {
}

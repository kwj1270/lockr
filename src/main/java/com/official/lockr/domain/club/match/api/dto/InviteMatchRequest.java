package com.official.lockr.domain.club.match.api.dto;

import com.official.lockr.domain.club.match.application.command.InviteMatchCommand;

import java.time.LocalDateTime;

public record InviteMatchRequest(
        String awayClubId,
        LocalDateTime matchDateTime,
        String location
) {
    public InviteMatchCommand toCommand(
            final String clubId,
            final String clubManagerUserId
    ) {
        return new InviteMatchCommand(clubManagerUserId, clubId, awayClubId, matchDateTime, location);
    }
}

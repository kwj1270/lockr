package com.official.lockr.domain.club.match.api.dto;

import com.official.lockr.domain.club.match.application.command.InviteMatchCommand;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record InviteMatchRequest(
        String awayClubId,
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime matchDateTime,
        String location
) {
    public InviteMatchCommand toCommand(
            final String clubId,
            final String clubManagerUserId
    ) {
        return new InviteMatchCommand(clubId, clubManagerUserId, awayClubId, matchDateTime, location);
    }
}

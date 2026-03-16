package com.official.lockr.domain.club.club.application.command;

public record LeaveClubCommand(
        String clubId,
        String userId
) {
}

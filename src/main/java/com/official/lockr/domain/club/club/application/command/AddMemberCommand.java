package com.official.lockr.domain.club.club.application.command;

public record AddMemberCommand(
        String clubId,
        String userId
) {
}

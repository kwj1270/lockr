package com.official.lockr.domain.club.team.application.command;

public record AddMemberCommand(
        String teamId,
        String userId
) {
}

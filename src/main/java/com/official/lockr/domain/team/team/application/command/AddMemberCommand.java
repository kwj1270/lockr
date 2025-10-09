package com.official.lockr.domain.team.team.application.command;

public record AddMemberCommand(
        String teamId,
        String userId
) {
}

package com.official.lockr.domain.team.team.application.command;

public record RegisterTeamCommand(
        String userId,
        String name,
        String description
) {

}

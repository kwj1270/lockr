package com.official.lockr.domain.team.team.application.command;

public record FoundTeamCommand(
        String userId,
        String name,
        String description
) {

}

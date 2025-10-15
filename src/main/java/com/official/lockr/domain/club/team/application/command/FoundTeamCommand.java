package com.official.lockr.domain.club.team.application.command;

public record FoundTeamCommand(
        String userId,
        String name,
        String description
) {

}

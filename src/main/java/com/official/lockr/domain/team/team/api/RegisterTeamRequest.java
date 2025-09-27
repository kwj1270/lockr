package com.official.lockr.domain.team.team.api;

import com.official.lockr.domain.team.team.application.command.RegisterTeamCommand;

public record RegisterTeamRequest(
        String name,
        String description
) {
    public RegisterTeamCommand toCommand(final String userId) {
        return new RegisterTeamCommand(userId, name, description);
    }
}

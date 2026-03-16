package com.official.lockr.domain.team.team.api.dto;

import com.official.lockr.domain.team.team.application.command.FoundTeamCommand;

public record FoundTeamRequest(
        String name,
        String description
) {
    public FoundTeamCommand toCommand(final String userId) {
        return new FoundTeamCommand(userId, name, description);
    }
}

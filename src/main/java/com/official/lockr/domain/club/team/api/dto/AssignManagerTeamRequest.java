package com.official.lockr.domain.club.team.api.dto;

import com.official.lockr.domain.club.team.application.command.AssignManagerCommand;

public record AssignManagerTeamRequest(
        String targetMemberId
) {

    public AssignManagerCommand toCommand(final String teamId, final String userId) {
        return new AssignManagerCommand(teamId, userId, targetMemberId);
    }
}

package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.AssignCoachCommand;

public record AssignCoachRequest(
        String targetUserId
) {

    public AssignCoachCommand toCommand(final String clubId, final String userId) {
        return new AssignCoachCommand(clubId, userId, targetUserId);
    }
}

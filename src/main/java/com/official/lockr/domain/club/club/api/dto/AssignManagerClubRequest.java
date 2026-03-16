package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.application.command.AssignManagerCommand;

public record AssignManagerClubRequest(
        String targetMemberId
) {

    public AssignManagerCommand toCommand(final String clubId, final String userId) {
        return new AssignManagerCommand(clubId, userId, targetMemberId);
    }
}

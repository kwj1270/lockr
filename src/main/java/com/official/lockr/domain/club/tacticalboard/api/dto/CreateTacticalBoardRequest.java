package com.official.lockr.domain.club.tacticalboard.api.dto;

import com.official.lockr.domain.club.tacticalboard.application.dto.CreateTacticalBoardCommand;

public record CreateTacticalBoardRequest(
        String name
) {
    public CreateTacticalBoardCommand toCommand(final String clubId, final String coachUserId) {
        return new CreateTacticalBoardCommand(clubId, coachUserId, name);
    }
}

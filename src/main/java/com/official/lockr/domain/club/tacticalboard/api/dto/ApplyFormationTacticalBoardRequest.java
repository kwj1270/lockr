package com.official.lockr.domain.club.tacticalboard.api.dto;

import com.official.lockr.domain.club.tacticalboard.application.dto.ApplyFormationTacticalBoardCommand;

public record ApplyFormationTacticalBoardRequest(
        String formationName
) {
    public ApplyFormationTacticalBoardCommand toCommand(
            final String clubId,
            final String tacticalBoardId,
            final String coachUserId
    ) {
        return new ApplyFormationTacticalBoardCommand(
                clubId,
                tacticalBoardId,
                coachUserId,
                formationName
        );
    }
}

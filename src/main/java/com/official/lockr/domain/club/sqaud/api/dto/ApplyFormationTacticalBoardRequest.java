package com.official.lockr.domain.club.sqaud.api.dto;

import com.official.lockr.domain.club.sqaud.application.dto.ApplyFormationTacticalBoardCommand;

public record ApplyFormationTacticalBoardRequest(
        String formationName
) {
    public ApplyFormationTacticalBoardCommand toCommand(
            final String staffUserId,
            final String squadId,
            final String tacticalBoardId
    ) {
        return new ApplyFormationTacticalBoardCommand(
                staffUserId,
                squadId,
                tacticalBoardId,
                formationName
        );
    }
}

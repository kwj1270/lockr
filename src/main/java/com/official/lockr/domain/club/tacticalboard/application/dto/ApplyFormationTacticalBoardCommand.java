package com.official.lockr.domain.club.tacticalboard.application.dto;

public record ApplyFormationTacticalBoardCommand(
        String clubId,
        String tacticalBoardId,
        String coachUserId,
        String formationName
) {
}

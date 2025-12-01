package com.official.lockr.domain.club.lineup.tacticalboard.application.dto;

public record ApplyFormationTacticalBoardCommand(
        String clubId,
        String tacticalBoardId,
        String coachUserId,
        String formationName
) {
}

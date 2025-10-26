package com.official.lockr.domain.club.board.application.dto;

public record ApplyFormationTacticalBoardCommand(
        String clubId,
        String tacticalBoardId,
        String coachUserId,
        String formationName
) {
}

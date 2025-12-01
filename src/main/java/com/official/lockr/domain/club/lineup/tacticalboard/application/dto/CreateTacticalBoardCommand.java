package com.official.lockr.domain.club.lineup.tacticalboard.application.dto;

public record CreateTacticalBoardCommand(
        String clubId,
        String coachUserId,
        String name
) {
}

package com.official.lockr.domain.club.board.application.dto;

public record CreateTacticalBoardCommand(
        String clubId,
        String coachUserId,
        String name
) {
}

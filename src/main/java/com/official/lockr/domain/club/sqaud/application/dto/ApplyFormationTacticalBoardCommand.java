package com.official.lockr.domain.club.sqaud.application.dto;

public record ApplyFormationTacticalBoardCommand(
        String staffUserId,
        String squadId,
        String tacticalBoardId,
        String formationName
) {
}

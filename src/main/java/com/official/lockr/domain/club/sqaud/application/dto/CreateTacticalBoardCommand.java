package com.official.lockr.domain.club.sqaud.application.dto;

public record CreateTacticalBoardCommand(
        String staffUserId,
        String name,
        String squadId
) {
}

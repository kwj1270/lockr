package com.official.lockr.domain.club.squad.application.dto;

public record CreateTacticalBoardCommand(
        String staffUserId,
        String name,
        String squadId
) {
}

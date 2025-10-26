package com.official.lockr.domain.club.sqaud.application.dto;

public record AddSquadPlayerCommand(
        String clubId,
        String userId,
        String memberId
) {
}

package com.official.lockr.domain.club.squad.application.dto;

public record AddSquadPlayerCommand(
        String teamId,
        String userId,
        String memberId
) {
}

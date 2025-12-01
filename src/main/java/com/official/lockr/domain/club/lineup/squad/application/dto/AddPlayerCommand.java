package com.official.lockr.domain.club.lineup.squad.application.dto;

public record AddPlayerCommand(
        String clubId,
        String userId,
        String memberId
) {
}

package com.official.lockr.domain.club.squard.application.dto;

public record AddPlayerCommand(
        String teamId,
        String userId,
        String memberId
) {
}

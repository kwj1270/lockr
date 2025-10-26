package com.official.lockr.domain.club.sqaud.application.dto;

public record AddPlayerCommand(
        String clubId,
        String userId,
        String memberId
) {
}

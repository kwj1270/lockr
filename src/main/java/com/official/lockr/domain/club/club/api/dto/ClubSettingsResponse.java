package com.official.lockr.domain.club.club.api.dto;

public record ClubSettingsResponse(
        boolean isPublic,
        String joinMethod
) {
}

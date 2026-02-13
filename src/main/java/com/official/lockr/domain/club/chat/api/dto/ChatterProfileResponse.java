package com.official.lockr.domain.club.chat.api.dto;

public record ChatterProfileResponse(
        String userId,
        String name,
        String profileImage
) {
}

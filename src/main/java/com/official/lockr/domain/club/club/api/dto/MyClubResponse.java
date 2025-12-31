package com.official.lockr.domain.club.club.api.dto;

public record MyClubResponse(
        String clubId,
        String name,
        String sportType,
        int memberCount,
        String city,
        String district,
        String profileImageUrl,
        String memberRole
) {
}

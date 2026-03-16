package com.official.lockr.domain.club.club.api.dto;

import java.time.LocalDateTime;

public record MemberResponse(
        String userId,
        String name,
        String role,
        LocalDateTime createdAt,
        String profileImageUrl,
        String position,
        Integer backNumber,
        String phone
) {
}
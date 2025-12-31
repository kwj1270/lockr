package com.official.lockr.domain.club.club.api.dto;

import com.official.lockr.domain.club.club.domain.MemberRole;

import java.time.LocalDateTime;

public record MyMemberInfoResponse(
        String memberId,
        String userId,
        String clubId,
        MemberRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
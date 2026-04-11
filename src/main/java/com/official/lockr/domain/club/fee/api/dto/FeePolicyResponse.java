package com.official.lockr.domain.club.fee.api.dto;

import java.time.LocalDateTime;

public record FeePolicyResponse(
        String id,
        String clubId,
        int amount,
        int dueDay,
        String bankName,
        String accountNumber,
        String accountHolder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

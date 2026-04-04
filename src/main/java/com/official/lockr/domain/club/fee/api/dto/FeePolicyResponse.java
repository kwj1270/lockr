package com.official.lockr.domain.club.fee.api.dto;

public record FeePolicyResponse(
        String id,
        String clubId,
        int amount,
        int dueDay,
        String bankName,
        String accountNumber,
        String accountHolder
) {
}

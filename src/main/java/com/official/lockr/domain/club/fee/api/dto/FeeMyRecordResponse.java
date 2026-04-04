package com.official.lockr.domain.club.fee.api.dto;

public record FeeMyRecordResponse(
        int year,
        int month,
        String status,
        String memo,
        double paidRate
) {
}

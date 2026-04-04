package com.official.lockr.domain.club.fee.api.dto;

public record FeeRecordResponse(
        String memberId,
        String memberName,
        int year,
        int month,
        String status,
        String memo
) {
}

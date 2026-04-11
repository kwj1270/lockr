package com.official.lockr.domain.club.fee.api.dto;

import java.time.LocalDateTime;

public record FeeRecordResponse(
        String memberId,
        String memberName,
        int year,
        int month,
        String status,
        String memo,
        LocalDateTime paidAt,
        String updatedBy
) {
}

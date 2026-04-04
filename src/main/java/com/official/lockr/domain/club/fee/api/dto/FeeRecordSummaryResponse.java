package com.official.lockr.domain.club.fee.api.dto;

import java.util.List;

public record FeeRecordSummaryResponse(
        List<FeeRecordResponse> records,
        int totalCount,
        int paidCount,
        double paidRate
) {
}

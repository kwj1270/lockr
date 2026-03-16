package com.official.lockr.domain.shorts.api.dto;

import java.time.LocalDateTime;

public record ShortsReportResponse(
        String id,
        String userId,
        String userName,
        String reason,
        String detail,
        LocalDateTime createdAt
) {
}

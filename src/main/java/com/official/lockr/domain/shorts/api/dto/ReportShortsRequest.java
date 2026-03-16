package com.official.lockr.domain.shorts.api.dto;

import com.official.lockr.domain.shorts.application.command.ReportShortsCommand;
import com.official.lockr.domain.shorts.domain.ReportReason;

public record ReportShortsRequest(String reason, String detail, String clubId) {

    public ReportShortsCommand toCommand(final String shortsId, final String userId) {
        return new ReportShortsCommand(shortsId, userId, clubId, ReportReason.valueOf(reason), detail);
    }
}

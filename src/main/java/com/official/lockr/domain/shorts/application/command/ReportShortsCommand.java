package com.official.lockr.domain.shorts.application.command;

import com.official.lockr.domain.shorts.domain.ReportReason;

public record ReportShortsCommand(
        String shortsId,
        String userId,
        String clubId,
        ReportReason reason,
        String detail
) {
}

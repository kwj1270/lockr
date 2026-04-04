package com.official.lockr.domain.club.fee.application.command;

import com.official.lockr.domain.club.fee.domain.FeeStatus;
import jakarta.annotation.Nullable;

public record UpdateFeeRecordCommand(
        String clubId,
        String userId,
        String memberId,
        int year,
        int month,
        FeeStatus status,
        @Nullable String memo
) {
}

package com.official.lockr.domain.club.fee.api.dto;

import com.official.lockr.domain.club.fee.application.command.UpdateFeeRecordCommand;
import com.official.lockr.domain.club.fee.domain.FeeStatus;

public record UpdateFeeRecordRequest(
        int year,
        int month,
        FeeStatus status,
        String memo
) {
    public UpdateFeeRecordCommand toCommand(final String clubId, final String userId, final String memberId) {
        return new UpdateFeeRecordCommand(clubId, userId, memberId, year, month, status, memo);
    }
}

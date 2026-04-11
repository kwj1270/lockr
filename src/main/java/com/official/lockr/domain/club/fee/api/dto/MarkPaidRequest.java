package com.official.lockr.domain.club.fee.api.dto;

import com.official.lockr.domain.club.fee.application.command.MarkPaidFeeRecordCommand;

public record MarkPaidRequest(
        int year,
        int month
) {
    public MarkPaidFeeRecordCommand toCommand(final String clubId, final String userId, final String memberId) {
        return new MarkPaidFeeRecordCommand(clubId, userId, memberId, year, month);
    }
}

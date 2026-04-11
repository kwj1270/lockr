package com.official.lockr.domain.club.fee.api.dto;

import com.official.lockr.domain.club.fee.application.command.MarkUnpaidFeeRecordCommand;

public record MarkUnpaidRequest(
        int year,
        int month
) {
    public MarkUnpaidFeeRecordCommand toCommand(final String clubId, final String userId, final String memberId) {
        return new MarkUnpaidFeeRecordCommand(clubId, userId, memberId, year, month);
    }
}

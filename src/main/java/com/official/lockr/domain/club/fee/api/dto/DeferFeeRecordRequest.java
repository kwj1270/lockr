package com.official.lockr.domain.club.fee.api.dto;

import com.official.lockr.domain.club.fee.application.command.DeferFeeRecordCommand;

public record DeferFeeRecordRequest(
        int year,
        int month
) {
    public DeferFeeRecordCommand toCommand(final String clubId, final String userId, final String memberId) {
        return new DeferFeeRecordCommand(clubId, userId, memberId, year, month);
    }
}

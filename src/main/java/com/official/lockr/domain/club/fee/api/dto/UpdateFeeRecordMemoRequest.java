package com.official.lockr.domain.club.fee.api.dto;

import com.official.lockr.domain.club.fee.application.command.UpdateFeeRecordMemoCommand;

public record UpdateFeeRecordMemoRequest(
        int year,
        int month,
        String memo
) {
    public UpdateFeeRecordMemoCommand toCommand(final String clubId, final String userId, final String memberId) {
        return new UpdateFeeRecordMemoCommand(clubId, userId, memberId, year, month, memo);
    }
}

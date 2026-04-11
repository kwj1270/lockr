package com.official.lockr.domain.club.fee.application.command;

public record UpdateFeeRecordMemoCommand(
        String clubId,
        String userId,
        String memberId,
        int year,
        int month,
        String memo
) {
}

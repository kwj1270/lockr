package com.official.lockr.domain.club.fee.application.command;

public record MarkUnpaidFeeRecordCommand(
        String clubId,
        String userId,
        String memberId,
        int year,
        int month
) {
}

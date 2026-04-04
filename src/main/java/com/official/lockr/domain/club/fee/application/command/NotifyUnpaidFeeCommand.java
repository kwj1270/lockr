package com.official.lockr.domain.club.fee.application.command;

public record NotifyUnpaidFeeCommand(
        String clubId,
        String userId,
        int year,
        int month
) {
}

package com.official.lockr.domain.club.fee.api.dto;

import com.official.lockr.domain.club.fee.application.command.NotifyUnpaidFeeCommand;

public record NotifyUnpaidRequest(
        int year,
        int month
) {
    public NotifyUnpaidFeeCommand toCommand(final String clubId, final String requestedBy) {
        return new NotifyUnpaidFeeCommand(clubId, requestedBy, year, month);
    }
}

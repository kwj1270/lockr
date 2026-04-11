package com.official.lockr.domain.club.fee.application.usecase;

import com.official.lockr.domain.club.fee.application.command.MarkUnpaidFeeRecordCommand;

public interface MarkUnpaidFeeRecordUseCase {
    void markUnpaid(MarkUnpaidFeeRecordCommand command);
}

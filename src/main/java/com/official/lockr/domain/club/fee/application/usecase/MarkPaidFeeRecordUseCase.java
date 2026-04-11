package com.official.lockr.domain.club.fee.application.usecase;

import com.official.lockr.domain.club.fee.application.command.MarkPaidFeeRecordCommand;

public interface MarkPaidFeeRecordUseCase {
    void markPaid(MarkPaidFeeRecordCommand command);
}

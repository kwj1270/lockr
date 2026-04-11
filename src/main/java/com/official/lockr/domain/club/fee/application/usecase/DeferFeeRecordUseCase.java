package com.official.lockr.domain.club.fee.application.usecase;

import com.official.lockr.domain.club.fee.application.command.DeferFeeRecordCommand;

public interface DeferFeeRecordUseCase {
    void defer(DeferFeeRecordCommand command);
}

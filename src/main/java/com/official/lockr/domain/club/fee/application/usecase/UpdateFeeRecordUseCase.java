package com.official.lockr.domain.club.fee.application.usecase;

import com.official.lockr.domain.club.fee.application.command.UpdateFeeRecordCommand;

public interface UpdateFeeRecordUseCase {
    void updateRecord(UpdateFeeRecordCommand command);
}

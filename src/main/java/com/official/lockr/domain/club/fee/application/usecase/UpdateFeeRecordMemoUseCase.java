package com.official.lockr.domain.club.fee.application.usecase;

import com.official.lockr.domain.club.fee.application.command.UpdateFeeRecordMemoCommand;

public interface UpdateFeeRecordMemoUseCase {
    void updateMemo(UpdateFeeRecordMemoCommand command);
}

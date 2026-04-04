package com.official.lockr.domain.club.fee.application.usecase;

import com.official.lockr.domain.club.fee.application.command.NotifyUnpaidFeeCommand;

public interface NotifyUnpaidFeeUseCase {
    void notifyUnpaid(NotifyUnpaidFeeCommand command);
}

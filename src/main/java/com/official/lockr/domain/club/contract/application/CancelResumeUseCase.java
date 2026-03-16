package com.official.lockr.domain.club.contract.application;

import com.official.lockr.domain.club.contract.application.command.CancelResumeCommand;

public interface CancelResumeUseCase {
    void cancel(final CancelResumeCommand cancelResumeCommand);
}

package com.official.lockr.domain.club.fee.application.usecase;

import com.official.lockr.domain.club.fee.application.command.SetFeePolicyCommand;
import com.official.lockr.domain.club.fee.domain.FeePolicy;

public interface SetFeePolicyUseCase {
    FeePolicy setPolicy(SetFeePolicyCommand command);
}

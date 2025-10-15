package com.official.lockr.domain.club.contract.application;

import com.official.lockr.domain.club.contract.application.command.SignRepresentativeContractCommand;
import com.official.lockr.domain.club.contract.domain.Contract;

public interface SignRepresentativeContractUseCase {
    Contract sign(SignRepresentativeContractCommand signRepresentativeContractCommand);
}

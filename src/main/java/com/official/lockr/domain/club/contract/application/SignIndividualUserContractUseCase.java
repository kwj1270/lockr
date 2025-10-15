package com.official.lockr.domain.club.contract.application;

import com.official.lockr.domain.club.contract.application.command.SignIndividualUserCommand;
import com.official.lockr.domain.club.contract.domain.Contract;

public interface SignIndividualUserContractUseCase {
    Contract sign(SignIndividualUserCommand signIndividualUserCommand);
}

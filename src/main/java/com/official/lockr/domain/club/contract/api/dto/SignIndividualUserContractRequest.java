package com.official.lockr.domain.club.contract.api.dto;

import com.official.lockr.domain.club.contract.application.command.SignIndividualUserCommand;

public record SignIndividualUserContractRequest(
        String contractId,
        boolean agree
) {

    public SignIndividualUserCommand toCommand(final String teamId, final String userId) {
        return new SignIndividualUserCommand(contractId, teamId, userId, agree);
    }
}

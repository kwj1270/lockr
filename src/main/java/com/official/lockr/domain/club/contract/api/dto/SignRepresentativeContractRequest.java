package com.official.lockr.domain.club.contract.api.dto;

import com.official.lockr.domain.club.contract.application.command.SignRepresentativeContractCommand;

public record SignRepresentativeContractRequest(
        String resumeId,
        boolean agree
) {
    public SignRepresentativeContractCommand toCommand(final String teamId, final String userId) {
        return new SignRepresentativeContractCommand(
                teamId, userId, resumeId, agree
        );
    }
}

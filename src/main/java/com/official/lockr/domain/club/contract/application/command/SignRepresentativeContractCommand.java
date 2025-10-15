package com.official.lockr.domain.club.contract.application.command;

public record SignRepresentativeContractCommand(
        String teamId,
        String userId,
        String resumeId,
        boolean agree
) {
}

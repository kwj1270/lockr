package com.official.lockr.domain.club.contract.application.command;

public record SignRepresentativeContractCommand(
        String clubId,
        String userId,
        String resumeId,
        boolean agree
) {
}

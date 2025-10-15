package com.official.lockr.domain.club.contract.application.command;

public record SignIndividualUserCommand(
        String contractId,
        String teamId,
        String userId,
        boolean agree
) {
}

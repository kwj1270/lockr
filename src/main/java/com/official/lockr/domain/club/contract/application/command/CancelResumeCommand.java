package com.official.lockr.domain.club.contract.application.command;

public record CancelResumeCommand(
        String clubId,
        String userId
) {
}

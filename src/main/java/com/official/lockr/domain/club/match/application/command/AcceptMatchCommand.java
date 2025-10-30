package com.official.lockr.domain.club.match.application.command;

public record AcceptMatchCommand(
        String matchProposeId,
        String userId,
        String clubId,
        boolean accept
) {
}

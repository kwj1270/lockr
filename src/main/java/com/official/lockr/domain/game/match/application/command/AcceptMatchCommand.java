package com.official.lockr.domain.game.match.application.command;

public record AcceptMatchCommand(
        String matchProposeId,
        String userId,
        String teamId,
        boolean accept
) {
}

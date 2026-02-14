package com.official.lockr.domain.club.sport.football.squad.application.command;

public record CreateSquadCommand(
        String clubId,
        String userId
) {
}

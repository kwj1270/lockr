package com.official.lockr.domain.club.sport.football.squad.application.command;

public record RemoveSquadPlayerCommand(
        String clubId,
        String userId
) {
}

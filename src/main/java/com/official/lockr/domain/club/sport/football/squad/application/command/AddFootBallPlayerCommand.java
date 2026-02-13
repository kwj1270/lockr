package com.official.lockr.domain.club.sport.football.squad.application.command;

public record AddFootBallPlayerCommand(
        String clubId,
        String userId
) {
}

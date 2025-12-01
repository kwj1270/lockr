package com.official.lockr.domain.club.recruitment.applications.application.command;

public record CancelApplicationCommand(
        String clubId,
        String tryoutId,
        String userId
) {
}

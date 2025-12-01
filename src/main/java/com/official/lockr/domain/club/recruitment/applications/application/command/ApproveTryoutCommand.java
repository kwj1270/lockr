package com.official.lockr.domain.club.recruitment.applications.application.command;

public record ApproveTryoutCommand(
        String clubId,
        String tryoutId,
        String processedByUserId
) {
}

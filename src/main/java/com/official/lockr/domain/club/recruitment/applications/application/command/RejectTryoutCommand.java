package com.official.lockr.domain.club.recruitment.applications.application.command;

public record RejectTryoutCommand(
        String clubId,
        String tryoutId,
        String processedByUserId,
        String rejectReason
) {
}

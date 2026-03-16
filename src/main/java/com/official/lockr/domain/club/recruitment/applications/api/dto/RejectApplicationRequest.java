package com.official.lockr.domain.club.recruitment.applications.api.dto;

import com.official.lockr.domain.club.recruitment.applications.application.command.RejectTryoutCommand;

public record RejectApplicationRequest(
        String reason
) {
    public RejectTryoutCommand toCommand(final String clubId, final String tryoutId, final String processedByUserId) {
        return new RejectTryoutCommand(clubId, tryoutId, processedByUserId, reason);
    }
}

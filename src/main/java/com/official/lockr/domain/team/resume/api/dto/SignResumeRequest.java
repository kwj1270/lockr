package com.official.lockr.domain.team.resume.api.dto;

import com.official.lockr.domain.team.resume.application.command.SignResumeCommand;

public record SignResumeRequest(
        String resumeId
) {

    public SignResumeCommand toCommand(final String teamId, final String userId) {
        return new SignResumeCommand(resumeId, teamId, userId);
    }
}

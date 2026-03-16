package com.official.lockr.domain.team.resume.application.command;

public record SignResumeCommand(
        String resumeId,
        String teamId,
        String userId
) {
}

package com.official.lockr.domain.team.resume.application;

import com.official.lockr.domain.team.resume.application.command.ApplyResumeCommand;
import com.official.lockr.domain.team.resume.domain.Resume;

public interface ApplyResumeUseCase {
    Resume apply(ApplyResumeCommand applyResumeCommand);
}

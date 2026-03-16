package com.official.lockr.domain.club.contract.application;

import com.official.lockr.domain.club.contract.application.command.ApplyResumeCommand;
import com.official.lockr.domain.club.contract.domain.Resume;

public interface ApplyResumeUseCase {
    Resume apply(ApplyResumeCommand applyResumeCommand);
}

package com.official.lockr.domain.team.resume.application;

import com.official.lockr.domain.team.resume.application.command.SignResumeCommand;
import com.official.lockr.domain.team.resume.domain.Contract;

public interface SignResumeUseCase {
    Contract sign(SignResumeCommand signResumeCommand);
}

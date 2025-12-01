package com.official.lockr.domain.club.recruitment.applications.application.usecase;

import com.official.lockr.domain.club.recruitment.applications.application.command.SubmitTryoutCommand;
import com.official.lockr.domain.club.recruitment.applications.domain.Application;

public interface SubmitApplicationUseCase {
    Application submit(final SubmitTryoutCommand submitTryoutCommand);
}

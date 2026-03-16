package com.official.lockr.domain.club.recruitment.applications.application.usecase;

import com.official.lockr.domain.club.recruitment.applications.application.command.RejectTryoutCommand;
import com.official.lockr.domain.club.recruitment.applications.domain.Application;

public interface RejectApplicationUseCase {
    Application reject(final RejectTryoutCommand command);
}

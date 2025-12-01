package com.official.lockr.domain.club.recruitment.applications.application.usecase;

import com.official.lockr.domain.club.recruitment.applications.application.command.ApproveTryoutCommand;
import com.official.lockr.domain.club.recruitment.applications.domain.Application;

public interface ApproveApplicationUseCase {

    default Application approve(final String clubId, final String tryoutId, final String userId) {
        return approve(new ApproveTryoutCommand(clubId, tryoutId, userId));
    }

    Application approve(final ApproveTryoutCommand command);
}

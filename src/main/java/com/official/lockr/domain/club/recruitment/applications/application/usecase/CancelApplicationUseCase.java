package com.official.lockr.domain.club.recruitment.applications.application.usecase;

import com.official.lockr.domain.club.recruitment.applications.application.command.CancelApplicationCommand;
import com.official.lockr.domain.club.recruitment.applications.domain.Application;

public interface CancelApplicationUseCase {

    default Application cancel(final String clubId, final String applicationId, final String userId) {
        return cancel(new CancelApplicationCommand(clubId, applicationId, userId));
    }

    Application cancel(final CancelApplicationCommand cancelApplicationCommand);
}

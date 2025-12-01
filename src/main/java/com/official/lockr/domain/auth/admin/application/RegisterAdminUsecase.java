package com.official.lockr.domain.auth.admin.application;

import com.official.lockr.domain.auth.admin.application.command.RegisterAdminCommand;
import com.official.lockr.domain.auth.admin.domain.Admin;

public interface RegisterAdminUsecase {
    Admin register(final RegisterAdminCommand command);
}

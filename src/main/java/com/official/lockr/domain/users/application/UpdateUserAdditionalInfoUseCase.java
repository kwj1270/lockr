package com.official.lockr.domain.users.application;

import com.official.lockr.domain.users.application.command.UpdateUserAdditionalInfoCommand;
import com.official.lockr.domain.users.domain.Users;

public interface UpdateUserAdditionalInfoUseCase {
    Users updateAdditionalInfo(final UpdateUserAdditionalInfoCommand command);
}

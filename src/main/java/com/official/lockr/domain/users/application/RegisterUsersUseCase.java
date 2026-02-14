package com.official.lockr.domain.users.application;

import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.domain.Users;

public interface RegisterUsersUseCase {

    Users register(SaveUsersCommand command);
}

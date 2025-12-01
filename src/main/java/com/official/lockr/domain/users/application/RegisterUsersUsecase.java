package com.official.lockr.domain.users.application;

import com.official.lockr.domain.users.application.command.SaveUsersCommand;
import com.official.lockr.domain.users.domain.Users;

public interface RegisterUsersUsecase {

    Users register(SaveUsersCommand command);
}

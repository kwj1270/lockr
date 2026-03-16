package com.official.lockr.domain.users.application;

import com.official.lockr.domain.users.application.command.WithdrawUsersCommand;

public interface WithdrawUsersUsecase {
    void withdraw(WithdrawUsersCommand command);
}

package com.official.lockr.domain.auth.signin.application.usecase;

import com.official.lockr.domain.auth.signin.application.command.RegisterSignInCommand;
import com.official.lockr.domain.auth.signin.domain.SignIn;

public interface RegisterSignInUseCase {

    SignIn register(final RegisterSignInCommand command);

}

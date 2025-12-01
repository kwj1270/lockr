package com.official.lockr.domain.auth.signin.application.usecase;

import com.official.lockr.domain.auth.signin.application.command.RegisterSignInTokenCommand;
import com.official.lockr.domain.auth.signin.domain.SignInToken;

public interface RegisterSignInTokenUseCase {
    SignInToken register(final RegisterSignInTokenCommand command);
}

package com.official.lockr.domain.auth.signin.application.usecase;

import com.official.lockr.domain.auth.signin.application.command.RefreshSignInTokenCommand;
import com.official.lockr.domain.auth.signin.domain.SignInToken;

public interface RefreshSignInTokenUseCase {
    SignInToken refresh(final RefreshSignInTokenCommand command);
}

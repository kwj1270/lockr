package com.official.lockr.domain.auth.signin.api;

import com.official.lockr.domain.auth.signin.application.usecase.RegisterSignInTokenUseCase;
import com.official.lockr.domain.auth.signin.application.command.RegisterSignInTokenCommand;
import com.official.lockr.domain.auth.signin.domain.SignInToken;
import com.official.lockr.domain.auth.signin.domain.event.ProcessedSignInEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SignInTokenConsumer {

    private final RegisterSignInTokenUseCase registerSignInTokenUseCase;

    public SignInTokenConsumer(final RegisterSignInTokenUseCase registerSignInTokenUseCase) {
        this.registerSignInTokenUseCase = registerSignInTokenUseCase;
    }

    @TransactionalEventListener
    public SignInToken consume(final ProcessedSignInEvent event) {
        return registerSignInTokenUseCase.register(new RegisterSignInTokenCommand(event.id(), event.userId()));
    }
}

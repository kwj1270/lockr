package com.official.lockr.domain.auth.signin.api;

import com.official.lockr.domain.auth.signin.application.usecase.RegisterSignInTokenUseCase;
import com.official.lockr.domain.auth.signin.application.command.RegisterSignInTokenCommand;
import com.official.lockr.domain.auth.signin.domain.event.ProcessedSignInEvent;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SignInTokenConsumer {

    private final RegisterSignInTokenUseCase registerSignInTokenUseCase;
    private final RetryTemplate retryTemplate;

    public SignInTokenConsumer(final RegisterSignInTokenUseCase registerSignInTokenUseCase) {
        this.registerSignInTokenUseCase = registerSignInTokenUseCase;
        this.retryTemplate = RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(1000, 1.5, 5000)
                .retryOn(Exception.class)
                .build();
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void consume(final ProcessedSignInEvent event) {
        final RegisterSignInTokenCommand command = new RegisterSignInTokenCommand(event.id(), event.userId());
        retryTemplate.execute(_ -> {
            registerSignInTokenUseCase.register(command);
            return null;
        });
    }
}
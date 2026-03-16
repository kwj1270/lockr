package com.official.lockr.domain.auth.signin.api;

import com.official.lockr.domain.auth.oidc.domain.OidcRepository;
import com.official.lockr.domain.auth.signin.domain.SignInTokenRepository;
import com.official.lockr.domain.users.domain.event.WithdrawnUserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WithdrawnUserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(WithdrawnUserEventConsumer.class);

    private final OidcRepository oidcRepository;
    private final SignInTokenRepository signInTokenRepository;
    private final RetryTemplate retryTemplate;

    public WithdrawnUserEventConsumer(
            final OidcRepository oidcRepository,
            final SignInTokenRepository signInTokenRepository,
            final RetryTemplate retryTemplate
    ) {
        this.oidcRepository = oidcRepository;
        this.signInTokenRepository = signInTokenRepository;
        this.retryTemplate = retryTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void consume(final WithdrawnUserEvent event) {
        retryTemplate.execute(_ -> {
            log.info("Withdrawing user auth data: userId={}", event.userId());
            oidcRepository.deleteByUserId(event.userId());
            signInTokenRepository.deleteByUserId(event.userId());
            return null;
        });
    }
}

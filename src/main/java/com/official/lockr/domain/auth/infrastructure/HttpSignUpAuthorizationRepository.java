package com.official.lockr.domain.auth.infrastructure;

import com.official.lockr.domain.auth.domain.auth.SignUpAuthorization;
import com.official.lockr.domain.auth.domain.auth.SignUpAuthorizationRepository;
import com.official.lockr.domain.auth.infrastructure.http.HttpSignUpAuthorizationClient;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public class HttpSignUpAuthorizationRepository implements SignUpAuthorizationRepository {

    private final HttpSignUpAuthorizationClient httpSignUpAuthorizationClient;

    public HttpSignUpAuthorizationRepository(final HttpSignUpAuthorizationClient httpSignUpAuthorizationClient) {
        this.httpSignUpAuthorizationClient = httpSignUpAuthorizationClient;
    }

    @Nullable
    @Override
    public SignUpAuthorization find(final String providerId, final String providerType) {
        return httpSignUpAuthorizationClient.find(providerId, providerType);
    }

    @Override
    public SignUpAuthorization save(final String providerId, final String providerType) {
        return httpSignUpAuthorizationClient.save(providerId, providerType);
    }
}

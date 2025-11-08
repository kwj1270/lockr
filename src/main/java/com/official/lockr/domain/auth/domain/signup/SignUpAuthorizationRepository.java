package com.official.lockr.domain.auth.domain.signup;

import jakarta.annotation.Nullable;

public interface SignUpAuthorizationRepository {
    @Nullable
    SignUpAuthorization find(String providerId, String provider);

    SignUpAuthorization save(String providerId, String provider);
}

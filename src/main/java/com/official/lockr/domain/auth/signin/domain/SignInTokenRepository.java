package com.official.lockr.domain.auth.signin.domain;

import jakarta.annotation.Nullable;

import java.util.List;

public interface SignInTokenRepository {
    SignInToken save(SignInToken token);

    @Nullable
    SignInToken findByToken(String token);

    @Nullable
    SignInToken findById(String id);

    List<SignInToken> findBySignInId(String signInId);

    void deleteById(String id);
}

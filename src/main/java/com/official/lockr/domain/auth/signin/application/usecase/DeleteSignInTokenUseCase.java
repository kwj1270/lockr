package com.official.lockr.domain.auth.signin.application.usecase;

public interface DeleteSignInTokenUseCase {
    void delete(String userId);
}

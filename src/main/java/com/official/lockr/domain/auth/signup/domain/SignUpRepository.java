package com.official.lockr.domain.auth.signup.domain;

public interface SignUpRepository {
    SignUp save();

    void delete(String userId);
}

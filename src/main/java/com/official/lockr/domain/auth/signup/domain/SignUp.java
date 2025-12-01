package com.official.lockr.domain.auth.signup.domain;

import java.util.Objects;

public class SignUp {

    private final String userId;

    public SignUp(final String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final SignUp that = (SignUp) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}

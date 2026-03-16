package com.official.lockr.domain.auth.signin.api.dto;

public record SignInAdminHttpRequest(
        String id,
        String password
) {
}

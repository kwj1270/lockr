package com.official.lockr.domain.auth.admin.api.dto;

public record SignInAdminHttpRequest(
        String id,
        String password
) {
}

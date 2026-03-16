package com.official.lockr.domain.auth.signin.api.dto;

import java.time.LocalDateTime;

public record SignInTokenResponse(
        String token,
        LocalDateTime expiresAt
) {
}
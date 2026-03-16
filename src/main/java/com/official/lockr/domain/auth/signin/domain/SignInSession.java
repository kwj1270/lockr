package com.official.lockr.domain.auth.signin.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public record SignInSession(
        String userId,
        String deviceId, String deviceName, String deviceOS,
        String ipAddress, String userAgent,
        LocalDateTime createdAt
) implements Serializable {
}

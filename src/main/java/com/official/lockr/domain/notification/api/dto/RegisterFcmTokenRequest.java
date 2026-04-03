package com.official.lockr.domain.notification.api.dto;

import com.official.lockr.domain.notification.application.command.RegisterFcmTokenCommand;
import jakarta.validation.constraints.NotBlank;

public record RegisterFcmTokenRequest(
        @NotBlank String token,
        @NotBlank String deviceId
) {
    public RegisterFcmTokenCommand toCommand(final String userId) {
        return new RegisterFcmTokenCommand(userId, token, deviceId);
    }
}

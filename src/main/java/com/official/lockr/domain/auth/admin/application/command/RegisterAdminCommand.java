package com.official.lockr.domain.auth.admin.application.command;

public record RegisterAdminCommand(
        String id,
        String password
) {
}

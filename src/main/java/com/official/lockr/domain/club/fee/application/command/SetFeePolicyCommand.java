package com.official.lockr.domain.club.fee.application.command;

import jakarta.annotation.Nullable;

public record SetFeePolicyCommand(
        String clubId,
        String userId,
        int amount,
        int dueDay,
        @Nullable String bankName,
        @Nullable String accountNumber,
        @Nullable String accountHolder
) {
}

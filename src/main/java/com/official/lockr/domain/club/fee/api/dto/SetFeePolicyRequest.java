package com.official.lockr.domain.club.fee.api.dto;

import com.official.lockr.domain.club.fee.application.command.SetFeePolicyCommand;

public record SetFeePolicyRequest(
        int amount,
        int dueDay,
        String bankName,
        String accountNumber,
        String accountHolder
) {
    public SetFeePolicyCommand toCommand(final String clubId, final String userId) {
        return new SetFeePolicyCommand(clubId, userId, amount, dueDay, bankName, accountNumber, accountHolder);
    }
}

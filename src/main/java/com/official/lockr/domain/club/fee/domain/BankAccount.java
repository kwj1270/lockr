package com.official.lockr.domain.club.fee.domain;

import jakarta.annotation.Nullable;

public record BankAccount(
        String bankName,
        String accountNumber,
        String accountHolder
) {
    @Nullable
    public static BankAccount of(String bankName, String accountNumber, String accountHolder) {
        if (bankName == null && accountNumber == null && accountHolder == null) {
            return null;
        }
        return new BankAccount(bankName, accountNumber, accountHolder);
    }
}

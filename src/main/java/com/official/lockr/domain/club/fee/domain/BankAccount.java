package com.official.lockr.domain.club.fee.domain;

public record BankAccount(
        String bankName,
        String accountNumber,
        String accountHolder
) {
    /** @return null if all arguments are null */
    public static BankAccount of(String bankName, String accountNumber, String accountHolder) {
        if (bankName == null && accountNumber == null && accountHolder == null) {
            return null;
        }
        return new BankAccount(bankName, accountNumber, accountHolder);
    }
}

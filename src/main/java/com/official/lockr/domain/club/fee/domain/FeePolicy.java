package com.official.lockr.domain.club.fee.domain;

import com.official.lockr.domain.club.fee.domain.event.FeePolicyChangedEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class FeePolicy extends AggregateRoot {

    private final String id;
    private final String clubId;
    private int amount;
    private int dueDay;
    private BankAccount bankAccount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public FeePolicy(final String id, final String clubId, final int amount, final int dueDay,
                     final BankAccount bankAccount, final LocalDateTime createdAt, final LocalDateTime updatedAt) {
        this.id = id;
        this.clubId = clubId;
        this.amount = amount;
        this.dueDay = dueDay;
        this.bankAccount = bankAccount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FeePolicy init(final String clubId, final int amount, final int dueDay, final BankAccount bankAccount) {
        validateAmount(amount);
        validateDueDay(dueDay);

        final FeePolicy policy = new FeePolicy(
                generateUlid(), clubId, amount, dueDay, bankAccount,
                LocalDateTime.now(), LocalDateTime.now()
        );

        policy.addEvent(new FeePolicyChangedEvent(
                policy.id, policy.clubId, policy.amount, policy.dueDay, LocalDateTime.now()
        ));

        return policy;
    }

    public void updatePolicy(final int amount, final int dueDay, final BankAccount bankAccount) {
        validateAmount(amount);
        validateDueDay(dueDay);

        this.amount = amount;
        this.dueDay = dueDay;
        this.bankAccount = bankAccount;

        this.addEvent(new FeePolicyChangedEvent(
                this.id, this.clubId, this.amount, this.dueDay, LocalDateTime.now()
        ));
    }

    private static void validateAmount(final int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("회비 금액은 0원 이상이어야 합니다.");
        }
    }

    private static void validateDueDay(final int dueDay) {
        if (dueDay < 1 || dueDay > 28) {
            throw new IllegalArgumentException("납부 기한은 1~28일 범위여야 합니다.");
        }
    }

    public String getId() { return id; }
    public String getClubId() { return clubId; }
    public int getAmount() { return amount; }
    public int getDueDay() { return dueDay; }
    public BankAccount getBankAccount() { return bankAccount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final FeePolicy that = (FeePolicy) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}

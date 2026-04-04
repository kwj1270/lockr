package com.official.lockr.domain.club.fee.infrastructure;

import com.official.lockr.domain.club.fee.domain.BankAccount;
import com.official.lockr.domain.club.fee.domain.FeePolicy;
import com.official.lockr.domain.club.fee.domain.FeePolicyRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.FeePoliciesDao;
import org.jooq.generated.tables.pojos.FeePoliciesEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.jooq.generated.tables.FeePoliciesJOOQEntity.FEE_POLICIES;

@Repository
public class JOOQFeePolicyRepository implements FeePolicyRepository {

    private final FeePoliciesDao feePoliciesDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQFeePolicyRepository(final Configuration configuration,
                                   final DomainEventPublisher domainEventPublisher) {
        this.feePoliciesDao = new FeePoliciesDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    @Override
    public FeePolicy save(final FeePolicy policy) {
        final BankAccount bank = policy.getBankAccount();
        feePoliciesDao.ctx()
                .insertInto(FEE_POLICIES)
                .set(FEE_POLICIES.ID, policy.getId())
                .set(FEE_POLICIES.CLUB_ID, policy.getClubId())
                .set(FEE_POLICIES.AMOUNT, policy.getAmount())
                .set(FEE_POLICIES.DUE_DAY, policy.getDueDay())
                .set(FEE_POLICIES.BANK_NAME, bank != null ? bank.bankName() : null)
                .set(FEE_POLICIES.ACCOUNT_NUMBER, bank != null ? bank.accountNumber() : null)
                .set(FEE_POLICIES.ACCOUNT_HOLDER, bank != null ? bank.accountHolder() : null)
                .set(FEE_POLICIES.CREATED_AT, policy.getCreatedAt())
                .set(FEE_POLICIES.UPDATED_AT, policy.getUpdatedAt())
                .onDuplicateKeyUpdate()
                .set(FEE_POLICIES.AMOUNT, policy.getAmount())
                .set(FEE_POLICIES.DUE_DAY, policy.getDueDay())
                .set(FEE_POLICIES.BANK_NAME, bank != null ? bank.bankName() : null)
                .set(FEE_POLICIES.ACCOUNT_NUMBER, bank != null ? bank.accountNumber() : null)
                .set(FEE_POLICIES.ACCOUNT_HOLDER, bank != null ? bank.accountHolder() : null)
                .set(FEE_POLICIES.UPDATED_AT, policy.getUpdatedAt())
                .execute();
        policy.publish(domainEventPublisher);
        return policy;
    }

    @Nullable
    @Override
    public FeePolicy findByClubId(final String clubId) {
        final var record = feePoliciesDao.ctx()
                .selectFrom(FEE_POLICIES)
                .where(FEE_POLICIES.CLUB_ID.eq(clubId))
                .fetchOne();
        if (Objects.isNull(record)) {
            return null;
        }
        return domain(record.into(FeePoliciesEntity.class));
    }

    private static FeePolicy domain(final FeePoliciesEntity entity) {
        return new FeePolicy(
                entity.getId(),
                entity.getClubId(),
                entity.getAmount(),
                entity.getDueDay(),
                BankAccount.of(entity.getBankName(), entity.getAccountNumber(), entity.getAccountHolder()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

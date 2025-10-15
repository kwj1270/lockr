package com.official.lockr.domain.club.contract.infrastructure;

import com.official.lockr.domain.club.contract.domain.Contract;
import com.official.lockr.domain.club.contract.domain.ContractRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ContractsDao;
import org.jooq.generated.tables.pojos.ContractsEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.ContractsJOOQEntity.CONTRACTS;

@Repository
public class JOOQContractRepository implements ContractRepository {

    private final ContractsDao contractsDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQContractRepository(final Configuration configuration,
                                  final DomainEventPublisher domainEventPublisher) {
        this.contractsDao = new ContractsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public Contract find(final String id) {
        final ContractsEntity entity = contractsDao.findById(id);
        if (isNull(entity)) {
            return null;
        }
        return new Contract(
                entity.getId(),
                entity.getTeamId(),
                entity.getIndividualUserId(),
                entity.getIndividualUserAgree(),
                entity.getIndividualUserSignAt(),
                entity.getRepresentativeUserId(),
                entity.getRepresentativeRole(),
                entity.getRepresentativeAgree(),
                entity.getRepresentativeSignAt(),
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }

    @Transactional
    @Override
    public Contract save(final Contract contract) {
        upsert(contract);
        contract.publish(domainEventPublisher);
        return contract;
    }

    private int upsert(final Contract contract) {
        return contractsDao.ctx()
                .insertInto(CONTRACTS)
                .set(CONTRACTS.ID, contract.getId())
                .set(CONTRACTS.TEAM_ID, contract.getTeamId())
                .set(CONTRACTS.INDIVIDUAL_USER_ID, contract.getIndividualUserId())
                .set(CONTRACTS.INDIVIDUAL_USER_AGREE, contract.isIndividualUserAgree())
                .set(CONTRACTS.INDIVIDUAL_USER_SIGN_AT, contract.getIndividualUserSignedAt())
                .set(CONTRACTS.REPRESENTATIVE_USER_ID, contract.getRepresentativeUserId())
                .set(CONTRACTS.REPRESENTATIVE_ROLE, contract.getRepresentativeUserRole())
                .set(CONTRACTS.REPRESENTATIVE_AGREE, contract.isRepresentativeUserAgree())
                .set(CONTRACTS.REPRESENTATIVE_SIGN_AT, contract.getRepresentativeSignedAt())
                .set(CONTRACTS.CREATED_AT, contract.getCreatedAt())
                .set(CONTRACTS.DELETED_AT, contract.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(CONTRACTS.TEAM_ID, contract.getTeamId())
                .set(CONTRACTS.INDIVIDUAL_USER_ID, contract.getIndividualUserId())
                .set(CONTRACTS.INDIVIDUAL_USER_AGREE, contract.isIndividualUserAgree())
                .set(CONTRACTS.INDIVIDUAL_USER_SIGN_AT, contract.getIndividualUserSignedAt())
                .set(CONTRACTS.REPRESENTATIVE_USER_ID, contract.getRepresentativeUserId())
                .set(CONTRACTS.REPRESENTATIVE_ROLE, contract.getRepresentativeUserRole())
                .set(CONTRACTS.REPRESENTATIVE_AGREE, contract.isRepresentativeUserAgree())
                .set(CONTRACTS.REPRESENTATIVE_SIGN_AT, contract.getRepresentativeSignedAt())
                .set(CONTRACTS.DELETED_AT, contract.getDeletedAt())
                .execute();
    }
}

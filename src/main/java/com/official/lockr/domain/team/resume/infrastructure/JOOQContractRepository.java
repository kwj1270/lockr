package com.official.lockr.domain.team.resume.infrastructure;

import com.official.lockr.domain.team.resume.domain.Contract;
import com.official.lockr.domain.team.resume.domain.ContractRepository;
import com.official.lockr.domain.team.resume.domain.event.SignedContractEvent;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ContractsDao;
import org.jooq.generated.tables.pojos.ContractsEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JOOQContractRepository implements ContractRepository {

    private final ContractsDao contractsDao;
    private final ApplicationEventPublisher applicationEventPublisher;

    public JOOQContractRepository(final Configuration configuration,
                                  final ApplicationEventPublisher applicationEventPublisher) {
        this.contractsDao = new ContractsDao(configuration);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    @Override
    public Contract save(final Contract contract) {
        final ContractsEntity contractsEntity = new ContractsEntity(
                contract.getId(),
                contract.getTeamId(),
                contract.getIndividualUserId(),
                contract.getRepresentativeUserId(),
                contract.getRepresentativeUserRole(),
                contract.getCreatedAt(),
                contract.getDeletedAt()
        );
        contractsDao.insert(contractsEntity);
        return new Contract(
                contractsEntity.getId(),
                contractsEntity.getTeamId(),
                contractsEntity.getIndividualUserId(),
                contractsEntity.getRepresentativeUserId(),
                contractsEntity.getRepresentativeRole(),
                contractsEntity.getCreatedAt(),
                contractsEntity.getDeletedAt()
        );
    }
}

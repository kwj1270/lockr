package com.official.lockr.domain.club.contract.application;

import com.official.lockr.domain.club.contract.application.command.SignIndividualUserCommand;
import com.official.lockr.domain.club.contract.application.command.SignRepresentativeContractCommand;
import com.official.lockr.domain.club.contract.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.util.Objects.isNull;

@Service
public class IndividualUserContractService implements SignRepresentativeContractUseCase, SignIndividualUserContractUseCase {

    private final Representatives representatives;
    private final ResumeRepository resumeRepository;
    private final ContractRepository contractRepository;
    private final IndividualUsers individualUsers;

    public IndividualUserContractService(final Representatives representatives,
                                         final ResumeRepository resumeRepository,
                                         final ContractRepository contractRepository,
                                         final IndividualUsers individualUsers
    ) {
        this.representatives = representatives;
        this.resumeRepository = resumeRepository;
        this.contractRepository = contractRepository;
        this.individualUsers = individualUsers;
    }

    @Override
    public Contract sign(final SignRepresentativeContractCommand signRepresentativeContractCommand) {
        final Representative representative = representatives.find(signRepresentativeContractCommand.teamId(), signRepresentativeContractCommand.userId());
        if (isNull(representative)) {
            throw new IllegalArgumentException();
        }
        final Resume resume = resumeRepository.find(signRepresentativeContractCommand.resumeId());
        if (isNull(resume)) {
            throw new IllegalArgumentException();
        }
        return contractRepository.save(contract(representative, resume, signRepresentativeContractCommand.agree()));
    }

    @Override
    public Contract sign(final SignIndividualUserCommand signIndividualUserCommand) {
        final IndividualUser individualUser = individualUsers.find(signIndividualUserCommand.userId());
        if (isNull(individualUser)) {
            throw new IllegalArgumentException();
        }
        final Contract contract = contractRepository.find(signIndividualUserCommand.contractId());
        if (isNull(contract) || contract.isInvalid() || contract.isConcluded()) {
            throw new IllegalStateException();
        }
        contract.sign(signIndividualUserCommand.teamId(), signIndividualUserCommand.userId(), signIndividualUserCommand.agree());
        return contractRepository.save(contract);
    }

    private static Contract contract(final Representative representative, final Resume resume, final boolean representativeAgree) {
        return Contract.create(
                UUID.randomUUID().toString(),
                resume.getTeamId(), resume.getUserId(),
                representative.getUserId(), representative.getMemberRole(), representativeAgree
        );
    }
}

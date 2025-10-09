package com.official.lockr.domain.team.resume.application;

import com.github.f4b6a3.ulid.UlidCreator;
import com.official.lockr.domain.team.common.Foot;
import com.official.lockr.domain.team.common.Position;
import com.official.lockr.domain.team.resume.application.command.ApplyResumeCommand;
import com.official.lockr.domain.team.resume.application.command.SignResumeCommand;
import com.official.lockr.domain.team.resume.domain.*;
import com.official.lockr.domain.team.resume.domain.event.SignedContractEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class ResumeService implements ApplyResumeUseCase, SignResumeUseCase {

    private final Representatives representatives;
    private final ResumeRepository resumeRepository;
    private final ContractRepository contractRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ResumeService(final Representatives representatives, final ResumeRepository resumeRepository,
                         final ContractRepository contractRepository, final ApplicationEventPublisher applicationEventPublisher) {
        this.representatives = representatives;
        this.resumeRepository = resumeRepository;
        this.contractRepository = contractRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Resume apply(final ApplyResumeCommand applyResumeCommand) {
        final Resume existed = resumeRepository.findByUserId(applyResumeCommand.teamId(), applyResumeCommand.userId());
        if(Objects.nonNull(existed) && existed.isActive()) {
            throw new IllegalStateException();
        }
        return resumeRepository.save(resume(applyResumeCommand));
    }

    @Override
    public Contract sign(final SignResumeCommand signResumeCommand) {
        final Representative representative = representatives.find(signResumeCommand.teamId(), signResumeCommand.userId());
        verify(representative);
        final Resume resume = resumeRepository.find(signResumeCommand.resumeId());
        verify(resume);
        final Contract contract = contractRepository.save(contract(representative, resume));
        applicationEventPublisher.publishEvent(new SignedContractEvent(contract));
        return contract;
    }

    private static Contract contract(final Representative representative, final Resume resume) {
        return Contract.create(
                UUID.randomUUID().toString(),
                resume.getTeamId(),
                resume.getUserId(),
                representative.getUserId(),
                representative.getMemberRole()
        );
    }

    private static void verify(final Representative representative) {
        if (Objects.isNull(representative)) {
            throw new IllegalArgumentException();
        }
    }

    private static void verify(final Resume resume) {
        if (Objects.isNull(resume)) {
            throw new IllegalArgumentException();
        }
    }

    private static Resume resume(final ApplyResumeCommand applyResumeCommand) {
        return Resume.create(
                UlidCreator.getUlid().toString(),
                applyResumeCommand.teamId(),
                applyResumeCommand.userId(),
                applyResumeCommand.birth(),
                applyResumeCommand.weight(),
                applyResumeCommand.height(),
                applyResumeCommand.name(),
                applyResumeCommand.email(),
                applyResumeCommand.address(),
                applyResumeCommand.phone(),
                applyResumeCommand.emergencyContactPhone(),
                applyResumeCommand.nationality(),
                applyResumeCommand.preferredPosition().stream().map(Position::valueOf).toList(),
                Foot.valueOf(applyResumeCommand.foot()),
                applyResumeCommand.advantages(),
                applyResumeCommand.disadvantages()
        );
    }
}

package com.official.lockr.domain.club.contract.application;

import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;
import com.official.lockr.domain.club.contract.application.command.ApplyResumeCommand;
import com.official.lockr.domain.club.contract.application.command.CancelResumeCommand;
import com.official.lockr.domain.club.contract.domain.Resume;
import com.official.lockr.domain.club.contract.domain.ResumeRepository;
import com.official.lockr.global.util.UlidUtils;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class ResumeService implements ApplyResumeUseCase, CancelResumeUseCase {

    private final ResumeRepository resumeRepository;

    public ResumeService(final ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    @Override
    public Resume apply(final ApplyResumeCommand applyResumeCommand) {
        final Resume existed = resumeRepository.findByUserId(applyResumeCommand.clubId(), applyResumeCommand.userId());
        if (nonNull(existed) && existed.isActive()) {
            return existed;
        }
        return resumeRepository.save(resume(applyResumeCommand));
    }

    @Override
    public void cancel(final CancelResumeCommand cancelResumeCommand) {
        final Resume resume = resumeRepository.findByUserId(cancelResumeCommand.clubId(), cancelResumeCommand.userId());
        if (isNull(resume)) {
            return;
        }
        resume.cancel();
        resumeRepository.save(resume);
    }

    private static Resume resume(final ApplyResumeCommand applyResumeCommand) {
        return Resume.create(
                UlidUtils.generateUlid(),
                applyResumeCommand.clubId(),
                applyResumeCommand.userId(),
                applyResumeCommand.profileImage(),
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

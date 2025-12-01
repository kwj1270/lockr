package com.official.lockr.domain.club.recruitment.recruitment.application.usecase;

import com.official.lockr.domain.club.recruitment.recruitment.application.command.UpdateRecruitmentCommand;
import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;

public interface UpdateRecruitmentUseCase {
    Recruitment update(UpdateRecruitmentCommand command);
}

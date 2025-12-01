package com.official.lockr.domain.club.recruitment.recruitment.application.usecase;

import com.official.lockr.domain.club.recruitment.recruitment.application.command.PostRecruitmentCommand;
import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;

public interface PostRecruitmentUseCase {
    Recruitment post(PostRecruitmentCommand command);
}

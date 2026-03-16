package com.official.lockr.domain.club.recruitment.recruitment.application.usecase;

import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;

public interface ChangeRecruitmentStatusUseCase {
    Recruitment changeStatus(String clubId, String recruitmentId, String status, String userId);
}

package com.official.lockr.domain.club.recruitment.recruitment.application.usecase;

public interface DeleteRecruitmentUseCase {
    void delete(String clubId, String recruitmentId, String userId);
}

package com.official.lockr.domain.club.recruitment.recruitment.domain;

import jakarta.annotation.Nullable;

import java.util.List;

public interface RecruitmentRepository {

    @Nullable
    Recruitment findById(String id);

    @Nullable
    Recruitment findByClubId(String clubId);

    List<Recruitment> findAllPublicRecruitments();

    Recruitment save(Recruitment recruitment);
}

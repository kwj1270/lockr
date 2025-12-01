package com.official.lockr.domain.club.recruitment.applications.domain;

import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;
import jakarta.annotation.Nullable;

public interface ApplicationRepository {
    @Nullable
    Application find(final String id);

    @Nullable
    Application findByRecruitmentAndUser(final String recruitmentId, final String userId);

    Application save(final Application application);

    Application findByClubAndUser(final String clubId, final String userId);
}

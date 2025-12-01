package com.official.lockr.domain.club.recruitment.applications.infrastructure;

import com.official.lockr.domain.club.recruitment.applications.domain.Application;
import com.official.lockr.domain.club.recruitment.applications.domain.ApplicationRepository;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;


@Repository
public class JOOQApplicationRepository implements ApplicationRepository {

    @Nullable
    @Override
    public Application find(final String id) {
        return null;
    }

    @Nullable
    @Override
    public Application findByRecruitmentAndUser(final String recruitmentId, final String userId) {
        return null;
    }

    @Override
    public Application save(final Application application) {
        return null;
    }

    @Override
    public Application findByClubAndUser(final String clubId, final String userId) {
        return null;
    }
}

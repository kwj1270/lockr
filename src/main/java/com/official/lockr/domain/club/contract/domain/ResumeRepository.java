package com.official.lockr.domain.club.contract.domain;

import jakarta.annotation.Nullable;

public interface ResumeRepository {
    @Nullable Resume find(final String id);

    @Nullable Resume findByUserId(final String teamId, final String userId);

    Resume save(final Resume resume);
}

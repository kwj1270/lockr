package com.official.lockr.domain.shorts.domain;

import jakarta.annotation.Nullable;

public interface ShortsRepository {

    @Nullable
    Shorts findById(String id);

    Shorts save(Shorts shorts);
}

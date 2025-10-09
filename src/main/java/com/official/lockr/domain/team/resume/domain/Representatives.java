package com.official.lockr.domain.team.resume.domain;

import jakarta.annotation.Nullable;

public interface Representatives {
    @Nullable
    Representative find(final String teamId, final String userId);
}

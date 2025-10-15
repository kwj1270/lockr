package com.official.lockr.domain.club.contract.domain;

import jakarta.annotation.Nullable;

public interface Representatives {
    @Nullable
    Representative find(final String teamId, final String userId);
}

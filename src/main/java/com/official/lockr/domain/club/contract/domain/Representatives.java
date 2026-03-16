package com.official.lockr.domain.club.contract.domain;

import jakarta.annotation.Nullable;

public interface Representatives {
    @Nullable
    Representative find(final String clubId, final String userId);
}

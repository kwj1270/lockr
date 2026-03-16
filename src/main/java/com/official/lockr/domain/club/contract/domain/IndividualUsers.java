package com.official.lockr.domain.club.contract.domain;

import jakarta.annotation.Nullable;

public interface IndividualUsers {
    @Nullable IndividualUser find(final String id);
}

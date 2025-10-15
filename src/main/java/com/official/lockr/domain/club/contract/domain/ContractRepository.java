package com.official.lockr.domain.club.contract.domain;

import jakarta.annotation.Nullable;

public interface ContractRepository {
    @Nullable Contract find(String contractId);

    Contract save(final Contract contract);
}

package com.official.lockr.domain.team.resume.domain.event;

import com.official.lockr.domain.team.resume.domain.Contract;

import java.time.LocalDateTime;

public record SignedContractEvent(
        String id, String teamId, String individualUserId, String representativeUserId,
        String representativeUserRole, LocalDateTime createdAt, LocalDateTime deletedAt
) {
    public SignedContractEvent(final Contract contract) {
        this(
                contract.getId(), contract.getTeamId(), contract.getIndividualUserId(),
                contract.getRepresentativeUserId(), contract.getRepresentativeUserRole(),
                contract.getCreatedAt(), contract.getDeletedAt()
        );
    }
}

package com.official.lockr.domain.club.contract.domain.event;

import com.official.lockr.domain.club.contract.domain.Contract;
import com.official.lockr.global.ddd.DomainEvent;

import java.time.LocalDateTime;

public record ConcludedContractEvent(
        String id,
        String teamId,
        String individualUserId,
        boolean individualUserAgree,
        LocalDateTime individualUserSignedAt,
        String representativeUserId,
        String representativeUserRole,
        boolean representativeUserAgree,
        LocalDateTime representativeSignedAt,
        LocalDateTime createdAt,
        LocalDateTime deletedAt
) implements DomainEvent {
    public ConcludedContractEvent(final Contract contract) {
        this(
                contract.getId(), contract.getTeamId(), contract.getIndividualUserId(), contract.isIndividualUserAgree(), contract.getIndividualUserSignedAt(),
                contract.getRepresentativeUserId(), contract.getRepresentativeUserRole(), contract.isRepresentativeUserAgree(), contract.getRepresentativeSignedAt(),
                contract.getCreatedAt(), contract.getDeletedAt()
        );
    }
}

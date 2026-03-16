package com.official.lockr.domain.club.contract.domain;

import com.official.lockr.domain.club.contract.domain.event.ConcludedContractEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.Objects;

import static java.util.Objects.isNull;

public class Contract extends AggregateRoot {

    private final String id;
    private final String clubId;
    private final String individualUserId;
    private boolean individualUserAgree;
    private LocalDateTime individualUserSignedAt;
    private final String representativeUserId;
    private final String representativeUserRole;
    private final boolean representativeUserAgree;
    private final LocalDateTime representativeSignedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime deletedAt;

    public Contract(final String id,
                    final String clubId,
                    final String individualUserId,
                    final boolean individualUserAgree,
                    final LocalDateTime individualUserSignedAt,
                    final String representativeUserId,
                    final String representativeUserRole,
                    final boolean representativeUserAgree,
                    final LocalDateTime representativeSignedAt,
                    final LocalDateTime createdAt,
                    final LocalDateTime deletedAt) {
        this.id = id;
        this.clubId = clubId;
        this.individualUserId = individualUserId;
        this.individualUserAgree = individualUserAgree;
        this.individualUserSignedAt = individualUserSignedAt;
        this.representativeUserId = representativeUserId;
        this.representativeUserRole = representativeUserRole;
        this.representativeUserAgree = representativeUserAgree;
        this.representativeSignedAt = representativeSignedAt;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public String getIndividualUserId() {
        return individualUserId;
    }

    public String getRepresentativeUserId() {
        return representativeUserId;
    }

    public String getRepresentativeUserRole() {
        return representativeUserRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isInvalid() {
        return !(isActive());
    }

    public boolean isActive() {
        return representativeUserAgree && isNull(deletedAt);
    }

    public boolean isIndividualUserAgree() {
        return individualUserAgree;
    }

    public LocalDateTime getIndividualUserSignedAt() {
        return individualUserSignedAt;
    }

    public boolean isRepresentativeUserAgree() {
        return representativeUserAgree;
    }

    public LocalDateTime getRepresentativeSignedAt() {
        return representativeSignedAt;
    }

    public void sign(final String clubId, final String userId, final boolean agree) {
        if (!(this.clubId.equals(clubId) && this.individualUserId.equals(userId))) {
            throw new IllegalArgumentException();
        }
        this.individualUserAgree = agree;
        this.individualUserSignedAt = agree ? LocalDateTime.now() : null;
        if(isConcluded()) {
            this.addEvent(new ConcludedContractEvent(this));
        }
    }

    public boolean isConcluded() {
        return individualUserAgree && representativeUserAgree;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Contract contract = (Contract) o;
        return Objects.equals(getId(), contract.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public static Contract create(final String id,
                                  final String clubId,
                                  final String individualUserId,
                                  final String representativeUserId,
                                  final String representativeUserRole,
                                  final boolean representativeAgree
    ) {
        return new Contract(
                id, clubId,
                individualUserId, false, null,
                representativeUserId, representativeUserRole, representativeAgree, LocalDateTime.now(),
                LocalDateTime.now(), null
        );
    }
}

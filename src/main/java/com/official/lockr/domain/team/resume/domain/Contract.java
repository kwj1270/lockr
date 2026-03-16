package com.official.lockr.domain.team.resume.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Contract {

    private final String id;
    private final String teamId;
    private final String individualUserId;
    private final String representativeUserId;
    private final String representativeUserRole;
    private final LocalDateTime createdAt;
    private final LocalDateTime deletedAt;

    public Contract(final String id, final String teamId, final String individualUserId, final String representativeUserId, final String representativeUserRole, final LocalDateTime createdAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.teamId = teamId;
        this.individualUserId = individualUserId;
        this.representativeUserId = representativeUserId;
        this.representativeUserRole = representativeUserRole;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public String getTeamId() {
        return teamId;
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

    public static Contract create(final String id, final String teamId, final String individualUserId,
                           final String representativeUserId, final String representativeUserRole) {
        return new Contract(id, teamId, individualUserId, representativeUserId, representativeUserRole, LocalDateTime.now(), null);
    }
}

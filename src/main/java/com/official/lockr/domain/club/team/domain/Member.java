package com.official.lockr.domain.club.team.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Member {

    private final String id;
    private final String userId;
    private final MemberRole role;
    private final String teamId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Member(final String id, final String userId, final MemberRole role, final String teamId, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.teamId = teamId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public MemberRole getRole() {
        return role;
    }

    public String getTeamId() {
        return teamId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isSame(final String userId) {
        return this.userId.equals(userId);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static Member president(final String memberId, final String userId, final String teamId) {
        return new Member(memberId, userId, MemberRole.PRESIDENT, teamId, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public static Member player(final String memberId, final String userId, final String teamId) {
        return new Member(memberId, userId, MemberRole.PLAYER, teamId, LocalDateTime.now(), LocalDateTime.now(), null);
    }
}

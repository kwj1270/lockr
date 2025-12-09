package com.official.lockr.domain.club.club.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class Member {

    private final String id;
    private final String userId;
    private MemberRole role;
    private final String clubId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Member(final String id, final String userId, final MemberRole role, final String clubId, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.clubId = clubId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public boolean isEqual(final String id) {
        return this.id.equals(id);
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

    public String getClubId() {
        return clubId;
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

    public boolean isPresident() {
        return role.isPresident();
    }

    public boolean isManager() {
        return role.isManager();
    }

    public boolean isStaff() {
        return role.isStaff();
    }

    public void assignManager() {
        role = MemberRole.MANAGER;
    }

    public void assignCoach() {
        role = MemberRole.COACH;
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

    public static Member president(final String userId, final String clubId) {
        return new Member(generateUlid(), userId, MemberRole.PRESIDENT, clubId, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public static Member basic(final String userId, final String clubId) {
        return new Member(generateUlid(), userId, MemberRole.BASIC, clubId, LocalDateTime.now(), LocalDateTime.now(), null);
    }
}

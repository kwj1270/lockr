package com.official.lockr.domain.club.team.domain;

import com.official.lockr.domain.club.team.domain.event.AddedMemberEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team extends AggregateRoot {

    private final String id;
    private final String name;
    private final String description;
    private List<Member> members;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Team(final String id, final String name, final String description) {
        this(id, name, description, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public Team(final String id, final String name, final String description, final List<Member> members, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.members = members;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void addMember(final Member member) {
        members.add(member);
        addEvent(new AddedMemberEvent(member));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Member> getMembers() {
        return members;
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

    public boolean isExistedMember(final String userId) {
        return this.members.stream().anyMatch(it -> it.isSame(userId));
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Team team = (Team) o;
        return Objects.equals(getId(), team.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}

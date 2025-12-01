package com.official.lockr.domain.club.club.domain;

import com.official.lockr.domain.club.club.domain.event.AddedMemberEvent;
import com.official.lockr.domain.club.club.domain.event.FoundClubEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Club extends AggregateRoot {

    private final String id;
    private final String foundUserId;
    private final String name;
    private final String description;
    private final String region;
    private final String sportType;
    private final String profileImageUrl;
    private final String backgroundImageUrl;
    private List<Member> members;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Club(final String id, final String foundUserId, final String name, final String description, final String region, final String sportType, final String profileImageUrl, final String backgroundImageUrl) {
        this(id, foundUserId, name, description, region, sportType, profileImageUrl, backgroundImageUrl, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public Club(final String id, final String foundUserId, final String name, final String description, final String region, final String sportType, final String profileImageUrl, final String backgroundImageUrl, final List<Member> members, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.foundUserId = foundUserId;
        this.name = name;
        this.description = description;
        this.region = region;
        this.sportType = sportType;
        this.profileImageUrl = profileImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
        this.members = members;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void addMember(final Member member) {
        members.add(member);
        addEvent(new AddedMemberEvent(member));
    }

    public boolean isEqual(final String id) {
        return this.id.equals(id);
    }

    public String getId() {
        return id;
    }

    public String getFoundUserId() {
        return foundUserId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRegion() {
        return region;
    }

    public String getSportType() {
        return sportType;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
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

    public boolean isExistedUser(final String userId) {
        return members.stream().anyMatch(it -> it.isSame(userId));
    }

    public boolean isNotPresident(final String memberId) {
        return members.stream()
                .filter(it -> it.isEqual(memberId))
                .noneMatch(Member::isPresident);
    }

    public boolean hasNotMember(final String memberId) {
        return members.stream().noneMatch(it -> it.getId().equals(memberId));
    }

    public boolean hasNotUser(final String userId) {
        return members.stream().noneMatch(it -> it.isSame(userId));
    }

    public void assignCoach(final String memberId) {
        final Member member = members.stream()
                .filter(it -> it.isEqual(memberId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        member.assignCoach();
    }

    public void assignManger(final String memberId) {
        final Member member = members.stream()
                .filter(it -> it.isEqual(memberId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        member.assignManager();
    }

    public boolean isStaff(final String userId) {
        return members.stream()
                .filter(it -> it.isSame(userId))
                .anyMatch(Member::isStaff);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Club club = (Club) o;
        return Objects.equals(getId(), club.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public static Club init(final String id, final String foundUserId, final String name, final String description, final String region, final String sportType, final String profileImageUrl, final String backgroundImageUrl) {
        final Club club = new Club(id, foundUserId, name, description, region, sportType, profileImageUrl, backgroundImageUrl);
        club.addEvent(new FoundClubEvent(club.id, club.name, club.description, club.region, club.sportType, club.createdAt));
        return club;
    }
}

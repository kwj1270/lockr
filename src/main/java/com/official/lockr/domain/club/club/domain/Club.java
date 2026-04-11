package com.official.lockr.domain.club.club.domain;

import com.official.lockr.domain.club.club.domain.event.AddedClubMemberEvent;
import com.official.lockr.domain.club.club.domain.event.FoundClubEvent;
import com.official.lockr.domain.club.club.domain.event.RemovedClubMemberEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

public class Club extends AggregateRoot {

    private final String id;
    private final String foundUserId;
    private String name;
    private final String sportType;
    private String city;
    private String district;
    private String description;
    private String profileImageUrl;
    private String backgroundImageUrl;
    private boolean isPublic;
    private String joinMethod;
    private List<Member> members;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Club(final String id, final String foundUserId, final String name, final String sportType, final String city, final String district, final String description, final String profileImageUrl, final String backgroundImageUrl) {
        this(id, foundUserId, name, sportType, city, district, description, profileImageUrl, backgroundImageUrl, true, "APPROVAL_REQUIRED", new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public Club(final String id, final String foundUserId, final String name, final String sportType, final String city, final String district, final String description, final String profileImageUrl, final String backgroundImageUrl, final List<Member> members, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this(id, foundUserId, name, sportType, city, district, description, profileImageUrl, backgroundImageUrl, true, "APPROVAL_REQUIRED", members, createdAt, updatedAt, deletedAt);
    }

    public Club(final String id, final String foundUserId, final String name, final String sportType, final String city, final String district, final String description, final String profileImageUrl, final String backgroundImageUrl, final boolean isPublic, final String joinMethod, final List<Member> members, final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.foundUserId = foundUserId;
        this.name = name;
        this.sportType = sportType;
        this.city = city;
        this.district = district;
        this.description = description;
        this.profileImageUrl = profileImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
        this.isPublic = isPublic;
        this.joinMethod = joinMethod;
        this.members = members;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void addMember(final Member member) {
        members.add(member);
        this.addEvent(new AddedClubMemberEvent(member, sportType));
    }

    public void removeMember(final String userId) {
        if (isStaff(userId)) {
            throw new IllegalStateException("운영진은 탈퇴할 수 없습니다. 먼저 역할을 해제해주세요.");
        }
        members.removeIf(member -> member.isSame(userId));
        this.addEvent(new RemovedClubMemberEvent(this.id, userId));
    }

    public void kickMember(final String requestUserId, final String targetMemberId) {
        if (!isPresidency(requestUserId)) {
            throw new IllegalArgumentException("회장 또는 부회장만 멤버를 강퇴할 수 있습니다.");
        }
        final Member target = members.stream()
                .filter(it -> it.isEqual(targetMemberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));
        if (target.isSame(requestUserId)) {
            throw new IllegalArgumentException("자기 자신을 강퇴할 수 없습니다.");
        }
        removeMember(target.getUserId());
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

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
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

    public boolean isPublic() {
        return isPublic;
    }

    public String getJoinMethod() {
        return joinMethod;
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

    public boolean isPresident(final String userId) {
        return members.stream()
                .filter(it -> it.isSame(userId))
                .anyMatch(Member::isPresident);
    }

    public boolean isPresidency(final String userId) {
        return members.stream()
                .filter(it -> it.isSame(userId))
                .anyMatch(Member::isPresidency);
    }

    public boolean hasNotMember(final String userId) {
        return members.stream().noneMatch(it -> it.isSame(userId));
    }

    public boolean hasNotUser(final String userId) {
        return members.stream().noneMatch(it -> it.isSame(userId));
    }

    public void assignCoach(final String userId) {
        final Member member = members.stream()
                .filter(it -> it.isSame(userId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        member.assignCoach();
    }

    public void assignManger(final String userId) {
        final Member member = members.stream()
                .filter(it -> it.isSame(userId))
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

    public static Club init(final String foundUserId, final String name, final String sportType, final String city, final String district, final String description, final String profileImageUrl, final String backgroundImageUrl) {
        final Club club = new Club(generateUlid(), foundUserId, name, sportType, city, district, description, profileImageUrl, backgroundImageUrl);
        club.addEvent(new FoundClubEvent(club.id, club.foundUserId, club.name, club.sportType, club.city, club.district, club.description, club.createdAt));
        return club;
    }

    public void delegatePresident(final String currentPresidentUserId, final String targetUserId) {
        if (!isPresident(currentPresidentUserId)) {
            throw new IllegalArgumentException("회장만 회장을 위임할 수 있습니다.");
        }
        if (hasNotMember(targetUserId)) {
            throw new IllegalArgumentException("대상이 클럽 멤버가 아닙니다.");
        }
        final Member currentPresident = members.stream()
                .filter(it -> it.isSame(currentPresidentUserId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        final Member target = members.stream()
                .filter(it -> it.isSame(targetUserId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        currentPresident.assignBasic();
        target.assignPresident();
    }

    public void changeMemberRole(final String requesterId, final String targetMemberId, final MemberRole role) {
        if (!isPresidency(requesterId)) {
            throw new IllegalArgumentException("회장 또는 부회장만 역할을 변경할 수 있습니다.");
        }
        final Member target = members.stream()
                .filter(it -> it.isEqual(targetMemberId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        if (target.isPresident()) {
            throw new IllegalArgumentException("회장의 역할은 변경할 수 없습니다.");
        }
        target.changeRole(role);
    }

    public void changeVisibility(final String requesterId, final boolean isPublic) {
        if (!isPresidency(requesterId)) {
            throw new IllegalArgumentException("회장 또는 부회장만 공개 설정을 변경할 수 있습니다.");
        }
        this.isPublic = isPublic;
    }

    public void changeJoinMethod(final String requesterId, final String joinMethod) {
        if (!isPresidency(requesterId)) {
            throw new IllegalArgumentException("회장 또는 부회장만 가입 방식을 변경할 수 있습니다.");
        }
        this.joinMethod = joinMethod;
    }

    public void updateInfo(final String requesterId, final String name, final String description, final String city, final String district, final String profileImageUrl, final String backgroundImageUrl) {
        if (!isPresidency(requesterId)) {
            throw new IllegalArgumentException("회장 또는 부회장만 클럽 정보를 수정할 수 있습니다.");
        }
        this.name = name;
        this.description = description;
        this.city = city;
        this.district = district;
        this.profileImageUrl = profileImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
    }

    public void updateMemberProfileImage(final String userId, final String profileImage) {
        final Member member = members.stream()
                .filter(it -> it.isSame(userId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        member.updateProfileImage(profileImage);
    }

}
